import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * Main function role :
 * 1. create Universe of 2D grid of dimension r * c.
 * 2. Then create operation list
 * 3. Run these operations simultaneously by using executor thread pool
 *
 */
public class GameOfLife {

    public static void main(String[] args) {
        //Initialize Universe
        Universe universe = new Universe(5, 5);

        universe.printGrid();

        List<Operation> operations = List.of(
                new UnderPopulation(),
                new NextGeneration(),
                new Overcrowd(),
                new Reproduction()
        );

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        for(Operation op : operations){
            executorService.submit(() -> op.transition(universe));
        }

        executorService.shutdown();
    }
}

class Universe {

    //This field will use for locking mechanism for simultaneous operation.
    private final Object lock = new Object();
    private boolean[][] grid;

    public Universe(int row, int column){
        boolean[][] grid = new boolean[row][column];
        for(int i = 0; i < row; i++){
            for(int j = 0; j < column; j++){
                grid[i][j] = false;
            }
        }
        //Glider Patten from the Hint
        grid[row / 2][column / 2] = true;
        grid[row / 2 + 1][column / 2 + 1] = true;
        grid[row / 2 + 2][column / 2 - 1] = true;
        grid[row / 2 + 2][column / 2] = true;
        grid[row / 2 + 2][column / 2 + 1] = true;

        this.grid = grid;
    }

    public boolean[][] getGrid(){
        return grid;
    }

    public Object getLock(){
        return this.lock;
    }

    public void printGrid(){
        rowLine();
        for (boolean[] booleans : grid) {
            System.out.print(":");
            for (int j = 0; j < grid[0].length; j++) {
                System.out.print("  " + (booleans[j] ? 1 : 0) + "  :");
            }
            rowLine();
        }
    }

    public void rowLine(){
        System.out.println();
        for (int i = 0; i < grid[0].length; i++) {
            System.out.print(" -----");
        }
        System.out.println();
    }

    /**
     *
     * @return Neighbor alive count
     * To get this info there will be other option as well
     * Keep the count with each cell - By Map - this will get complicated
     * So i prefer the simple way
     *
     */
    public int getNeighborsAliveCount(int i, int j){
        int count = 0;

        int row = grid.length;
        int column = grid[0].length;

        //Checking neighbors alive of not and increasing the count based on this
        if(i-1 >= 0 && j-1 >=0 && grid[i-1][j-1])
            count++;

        if(i-1 >= 0 && grid[i-1][j])
            count++;

        if(i-1 >= 0 && j+1 < column && grid[i-1][j+1])
            count++;

        if(j-1 >= 0 && grid[i][j-1])
            count++;

        if(j+1 < column && grid[i][j+1])
            count++;

        if(i+1 < row && j-1 > 0 && grid[i+1][j-1])
            count++;

        if(i+1 < row && grid[i+1][j])
            count++;

        if(i+1 < row && j+1 < column && grid[i+1][j+1])
            count++;

        return count;
    }

}


/**
 *
 * Four Operations : UnderPopulation, NextGeneration, Overcrowd, Reproduction
 *
 */
interface Operation {

    void transition(Universe universe);

}

class UnderPopulation implements Operation {

    //Any live cell with fewer than two live neighbors dies as if caused by underpopulation.
    @Override
    public void transition(Universe universe) {
        final boolean[][] grid = universe.getGrid();
        final int rows = grid.length;
        final int cols = grid[0].length;

        synchronized (universe.getLock()) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    int liveNeighbors = universe.getNeighborsAliveCount(i, j);
                    if (grid[i][j] && liveNeighbors < 2) {
                        grid[i][j] = false;
                    }
                }
            }

            System.out.println("\nAfter UnderPopulation : ");
            universe.printGrid();
        }
    }
}

class NextGeneration implements Operation {

    //Any live cell with two or three live neighbors lives on to the next generation.
    @Override
    public void transition(Universe universe) {
        final boolean[][] grid = universe.getGrid();
        final int rows = grid.length;
        final int cols = grid[0].length;

        synchronized (universe.getLock()) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    int liveNeighbors = universe.getNeighborsAliveCount(i, j);
                    if (grid[i][j] && (liveNeighbors == 2 || liveNeighbors == 3)) {
                        grid[i][j] = true;
                    } else {
                        grid[i][j] = false;
                    }
                }
            }

            System.out.println("\nAfter NextGeneration : ");
            universe.printGrid();
        }
    }
}

class Overcrowd implements Operation {

    //Any live cell with more than three live neighbors dies, as if by overcrowding.
    @Override
    public void transition(Universe universe) {
        final boolean[][] grid = universe.getGrid();
        final int rows = grid.length;
        final int cols = grid[0].length;

        synchronized (universe.getLock()) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    int liveNeighbors = universe.getNeighborsAliveCount(i, j);
                    if (grid[i][j] && liveNeighbors > 3) {
                        grid[i][j] = false;
                    }
                }
            }

            System.out.println("\nAfter Overcrowd : ");
            universe.printGrid();
        }
    }
}

class Reproduction implements Operation {

    //Any dead cell with exactly three live neighbors becomes a live cell, as if by reproduction.
    @Override
    public void transition(Universe universe) {
        final boolean[][] grid = universe.getGrid();
        final int rows = grid.length;
        final int cols = grid[0].length;

        synchronized (universe.getLock()) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    int liveNeighbors = universe.getNeighborsAliveCount(i, j);
                    if (!grid[i][j] && liveNeighbors == 3) {
                        grid[i][j] = true;
                    }
                }
            }

            System.out.println("\nAfter Reproduction : ");
            universe.printGrid();
        }
    }

}