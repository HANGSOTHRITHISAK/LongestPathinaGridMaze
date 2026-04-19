import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class LongestPathMaze {

    // step 2: data structures for backtracking
    static char[][] grid;
    static boolean[][] visited;
    static int rows, cols;
    
    static int bestLength = 0;
    static ArrayList<int[]> bestPath = new ArrayList<>();
    static int totalOpenCells = 0;

    // direction arrays for up, down, left, right
    static int[] dx = {-1, 1, 0, 0};
    static int[] dy = {0, 0, -1, 1};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide a maze file. Usage: java LongestPathMaze <maze_file.txt>");
            return;
        }

        // step 1: maze representation (read from file)
        if (!loadMaze(args[0])) return;

        // step 5: run from every open cell
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == '.') {
                    visited = new boolean[rows][cols]; // reset visited for new start
                    ArrayList<int[]> currentPath = new ArrayList<>();
                    
                    // mark starting cell
                    visited[r][c] = true;
                    currentPath.add(new int[]{r, c});
                    
                    // start dfs (length starts at 1, remaining unvisited drops by 1)
                    dfs(r, c, 1, currentPath, totalOpenCells - 1);
                }
            }
        }

        // step 6: output
        System.out.println("Longest path length: " + bestLength);
        printBestPath();
    }

    // step 3: backtracking function
    static void dfs(int x, int y, int length, ArrayList<int[]> currentPath, int remainingUnvisited) {
        // update best if length > bestLength
        if (length > bestLength) {
            bestLength = length;
            bestPath = new ArrayList<>(currentPath); // deep copy the path
        }

        // step 4: pruning
        if (length + remainingUnvisited <= bestLength) {
            return; // skip this branch, it can never beat the current best
        }

        // for each neighbor (up, down, left, right)
        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];

            // if inside bounds, open, and not visited:
            if (nx >= 0 && nx < rows && ny >= 0 && ny < cols && grid[nx][ny] == '.' && !visited[nx][ny]) {
                // mark visited, add to currentPath
                visited[nx][ny] = true;
                currentPath.add(new int[]{nx, ny});
                
                // recurse
                dfs(nx, ny, length + 1, currentPath, remainingUnvisited - 1);
                
                // backtrack
                currentPath.remove(currentPath.size() - 1);
                visited[nx][ny] = false;
            }
        }
    }

    // helper: loads the maze from a text file
    static boolean loadMaze(String filename) {
        try {
            ArrayList<String> lines = new ArrayList<>();
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine().trim());
            }
            scanner.close();

            rows = lines.size();
            cols = lines.get(0).length();
            grid = new char[rows][cols];

            for (int r = 0; r < rows; r++) {
                String line = lines.get(r);
                for (int c = 0; c < cols; c++) {
                    grid[r][c] = line.charAt(c);
                    if (grid[r][c] == '.') {
                        totalOpenCells++;
                    }
                }
            }
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("Error: Could not find file " + filename);
            return false;
        }
    }

    // helper: prints the grid with the longest path marked as '*'
    static void printBestPath() {
        char[][] visualGrid = new char[rows][cols];
        
        // copy original grid
        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < cols; c++) {
                visualGrid[r][c] = grid[r][c];
            }
        }

        // mark the path
        for (int i = 0; i < bestPath.size(); i++) {
            int[] pos = bestPath.get(i);
            int r = pos[0];
            int c = pos[1];
            if (i == 0) visualGrid[r][c] = 'S'; // start
            else if (i == bestPath.size() - 1) visualGrid[r][c] = 'E'; // end
            else visualGrid[r][c] = '*'; // path
        }

        System.out.println("Grid Visualization:");
        for (int r = 0; r < rows; r++) {
            System.out.println(new String(visualGrid[r]));
        }
    }
}