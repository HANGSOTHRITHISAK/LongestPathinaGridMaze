package src;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class LongestPathMaze {

    // Step 2: Data structures for backtracking
    static char[][] grid;
    static boolean[][] visited;
    static int rows, cols;
    
    static int bestLength = 0;
    static ArrayList<int[]> bestPath = new ArrayList<>();
    static int totalOpenCells = 0;

    // Direction arrays for Up, Down, Left, Right
    static int[] dx = {-1, 1, 0, 0};
    static int[] dy = {0, 0, -1, 1};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide a maze file. Usage: java LongestPathMaze <maze_file.txt>");
            return;
        }

        // Step 1: Maze representation (Read from file)
        if (!loadMaze(args[0])) return;

        // Step 5: Run from every open cell
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == '.') {
                    visited = new boolean[rows][cols]; // Reset visited for new start
                    ArrayList<int[]> currentPath = new ArrayList<>();
                    
                    // Mark starting cell
                    visited[r][c] = true;
                    currentPath.add(new int[]{r, c});
                    
                    // Start DFS (length starts at 1, remaining unvisited drops by 1)
                    dfs(r, c, 1, currentPath, totalOpenCells - 1);
                }
            }
        }

        // Step 6: Output
        System.out.println("Longest path length: " + bestLength);
        printBestPath();
    }

    // Step 3: Backtracking function
    static void dfs(int x, int y, int length, ArrayList<int[]> currentPath, int remainingUnvisited) {
        // Update best if length > bestLength
        if (length > bestLength) {
            bestLength = length;
            bestPath = new ArrayList<>(currentPath); // Deep copy the path
        }

        // Step 4: Pruning
        if (length + remainingUnvisited <= bestLength) {
            return; // Skip this branch, it can never beat the current best
        }

        // For each neighbor (up, down, left, right)
        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];

            // If inside bounds, open, and not visited:
            if (nx >= 0 && nx < rows && ny >= 0 && ny < cols && grid[nx][ny] == '.' && !visited[nx][ny]) {
                // Mark visited, add to currentPath
                visited[nx][ny] = true;
                currentPath.add(new int[]{nx, ny});
                
                // Recurse
                dfs(nx, ny, length + 1, currentPath, remainingUnvisited - 1);
                
                // Backtrack
                currentPath.remove(currentPath.size() - 1);
                visited[nx][ny] = false;
            }
        }
    }

    // Helper: Loads the maze from a text file
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

    // Helper: Prints the grid with the longest path marked as '*'
    static void printBestPath() {
        char[][] visualGrid = new char[rows][cols];
        
        // Copy original grid
        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < cols; c++) {
                visualGrid[r][c] = grid[r][c];
            }
        }

        // Mark the path
        for (int i = 0; i < bestPath.size(); i++) {
            int[] pos = bestPath.get(i);
            int r = pos[0];
            int c = pos[1];
            if (i == 0) visualGrid[r][c] = 'S'; // Start
            else if (i == bestPath.size() - 1) visualGrid[r][c] = 'E'; // End
            else visualGrid[r][c] = '*'; // Path
        }

        System.out.println("Grid Visualization:");
        for (int r = 0; r < rows; r++) {
            System.out.println(new String(visualGrid[r]));
        }
    }
}