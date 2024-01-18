package MazeSolverDependencies;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class MazeGenerator {
    private Color[] color;
    private int speedSleep;
    private int blockSize;
    private int columns;
    private int rows;
    public int[][] maze;
    GraphicsContext g;
    final static int backgroundCode = 0;
    final static int wallCode = 1;
    final static int pathCode = 2;
    final static int emptyCode = 3;
    final static int visitedCode = 4;

    public MazeGenerator(Color[] color, GraphicsContext g, int rows, int columns, int blockSize, int speedSleep) {
        
        this.color = color;
        this.g = g;
        this.rows = rows;
        this.columns = columns;
        this.blockSize = blockSize;
        this.speedSleep = speedSleep;
    }

    public void makeMaze() {
        this.maze=new int[rows][columns];
        int i,j;
        int emptyCt = 0; // number of rooms
        int wallCt = 0;  // number of walls
        int[] wallrow = new int[(rows*columns)/2];  // position of walls between rooms
        int[] wallcol = new int[(rows*columns)/2];
        for (i = 0; i<rows; i++)  // start with everything being a wall
            for (j = 0; j < columns; j++)
                this.maze[i][j] = wallCode;
        for (i = 1; i<rows-1; i += 2)  { // make a grid of empty rooms
            for (j = 1; j<columns-1; j += 2) {
                emptyCt++;
                maze[i][j] = -emptyCt;  // each room is represented by a different negative number
                if (i < rows-2) {  // record info about wall below this room
                    wallrow[wallCt] = i+1;
                    wallcol[wallCt] = j;
                    wallCt++;
                }
                if (j < columns-2) {  // record info about wall to right of this room
                    wallrow[wallCt] = i;
                    wallcol[wallCt] = j+1;
                    wallCt++;
                }
            }
        }
        Platform.runLater( () -> {
            g.setFill( color[emptyCode] );
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    if (maze[r][c] < 0)
                        this.g.fillRect( c*blockSize, r*blockSize, blockSize, blockSize );
                }
            }
        });
        synchronized(this) {
            try { wait(1000); }
            catch (InterruptedException e) { }
        }
        int r;
        for (i=wallCt-1; i>0; i--) {
            r = (int)(Math.random() * i);  // choose a wall randomly and maybe tear it down
            tearDown(wallrow[r],wallcol[r]);
            wallrow[r] = wallrow[i];
            wallcol[r] = wallcol[i];
        }
        for (i=1; i<rows-1; i++)  // replace negative values in maze[][] with emptyCode
            for (j=1; j<columns-1; j++)
                if (maze[i][j] < 0)
                    maze[i][j] = emptyCode;
        synchronized(this) {
            try { wait(1000); }
            catch (InterruptedException e) { }
        }
    }

    private void tearDown(int row, int col) {
        if (row % 2 == 1 && maze[row][col-1] != maze[row][col+1]) {
                fill(row, col-1, maze[row][col-1], maze[row][col+1]);
                maze[row][col] = maze[row][col+1];
                drawSquare(row,col,emptyCode);
                synchronized(this) {
                    try { wait(speedSleep); }
                    catch (InterruptedException e) { }
                }
            }
            else if (row % 2 == 0 && maze[row-1][col] != maze[row+1][col]) {
                fill(row-1, col, maze[row-1][col], maze[row+1][col]);
                maze[row][col] = maze[row+1][col];
                drawSquare(row,col,emptyCode);
                synchronized(this) {
                    try { wait(speedSleep); }
                    catch (InterruptedException e) { }
                }
            }
    }

    public void fill(int row, int col, int replace, int replaceWith) {

        if (maze[row][col] == replace) {
            maze[row][col] = replaceWith;
            fill(row+1,col,replace,replaceWith);
            fill(row-1,col,replace,replaceWith);
            fill(row,col+1,replace,replaceWith);
            fill(row,col-1,replace,replaceWith);
        }
    }

    public void drawSquare(int row, int column, int colorCode) {
        Platform.runLater(() -> {
            this.g.setFill(color[colorCode]);
            int x = blockSize * column;
            int y = blockSize * row;
            this.g.fillRect(x, y, blockSize, blockSize);
        });
    }


}

