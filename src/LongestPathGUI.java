import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class LongestPathGUI extends JFrame {

    // --- core algorithm variables ---
    private char[][] grid;
    private int rows, cols;
    private int totalOpenCells;
    private int bestLength = 0;
    
    private List<int[]> currentPath = new ArrayList<>();
    private List<int[]> bestPath = new ArrayList<>();
    private boolean[][] visited;
    
    // --- threading & control ---
    private Thread solverThread;
    private volatile boolean isRunning = false;
    private int animationDelay = 50; // milliseconds

    // --- ui components ---
    private MazePanel mazePanel;
    private JLabel statusLabel;
    private JLabel bestLengthLabel;
    private JLabel currentLengthLabel;
    private JSlider speedSlider;
    private JComboBox<String> mazeSelector;
    private JButton startBtn;

    // direction arrays for up, down, left, right
    private final int[] dRow = {-1, 1, 0, 0};
    private final int[] dCol = {0, 0, -1, 1};

    public LongestPathGUI() {
        setTitle("Autonomous Robot Routing (Longest Path NP-Hard Simulator)");
        setSize(800, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. top status panel
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        topPanel.setBackground(new Color(40, 44, 52));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        bestLengthLabel = createStyledLabel("🏆 Best Route Found: 0", new Color(46, 204, 113));
        currentLengthLabel = createStyledLabel("🤖 Current Search Depth: 0", new Color(52, 152, 219));
        
        topPanel.add(bestLengthLabel);
        topPanel.add(currentLengthLabel);
        add(topPanel, BorderLayout.NORTH);

        // 2. center maze panel
        mazePanel = new MazePanel();
        add(mazePanel, BorderLayout.CENTER);

        // 3. bottom control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        controlPanel.setBackground(new Color(236, 240, 241));

        String[] mazes = {"4x4 Simple", "6x6 Open Room", "10x10 Stress Test", "25x25 Complex Maze"};
        mazeSelector = new JComboBox<>(mazes);
        mazeSelector.addActionListener(e -> loadSelectedMaze());

        startBtn = new JButton("▶ Start Simulation");
        startBtn.setFont(new Font("Arial", Font.BOLD, 14));
        startBtn.setBackground(new Color(46, 204, 113));
        startBtn.setForeground(Color.WHITE);
        startBtn.setFocusPainted(false);
        startBtn.addActionListener(this::toggleSimulation);

        speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 50);
        speedSlider.setBackground(new Color(236, 240, 241));
        speedSlider.addChangeListener(e -> animationDelay = speedSlider.getValue());
        
        controlPanel.add(new JLabel("Select Area:"));
        controlPanel.add(mazeSelector);
        controlPanel.add(startBtn);
        controlPanel.add(new JLabel("Fast"));
        controlPanel.add(speedSlider);
        controlPanel.add(new JLabel("Slow"));

        add(controlPanel, BorderLayout.SOUTH);

        // initialize first maze
        loadSelectedMaze();
    }

    private JLabel createStyledLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(color);
        return label;
    }

    // --- hardcoded mazes for easy demonstration ---
private void loadSelectedMaze() {
        stopSimulation();
        int index = mazeSelector.getSelectedIndex();
        
        String[] mapData;
        if (index == 0) { // 4x4 simple
            mapData = new String[] {
                "....",
                ".###",
                "....",
                "...."
            };
        } else if (index == 1) { // 6x6 open room
            mapData = new String[] {
                "......",
                ".##...",
                "..#.#.",
                "...#..",
                ".#....",
                "......"
            };
        } else if (index == 2) { // 10x10 stress test (open space is hard for np-hard)
            mapData = new String[] {
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                "..........",
                ".........."
            };
        } else { // 25x25 complex maze (corridors make it solveable)
            mapData = new String[] {
                ".........................",
                ".#######################.",
                ".......................#.",
                ".#####################.#.",
                ".#.....................#.",
                ".#.#####################.",
                ".#.#.....................",
                ".#.#.###################.",
                ".#.#.#.................#.",
                ".#.#.#.###############.#.",
                ".#.#.#.#...............#.",
                ".#.#.#.#.#############.#.",
                ".#.#.#.#.#...........#.#.",
                ".#.#.#.#.#.#########.#.#.",
                ".#.#.#.#.#.#.......#.#.#.",
                ".#.#.#.#.#.#.#####.#.#.#.",
                ".#.#.#.#.#.#.#...#.#.#.#.",
                ".#.#.#.#.#.#.#.#.#.#.#.#.",
                ".#.#.#.#.#.#.#...#.#.#.#.",
                ".#.#.#.#.#.#.#####.#.#.#.",
                ".........................",
                "#######################..",
                ".........................",
                ".........................",
                "........................."
            };
        }

        rows = mapData.length;
        cols = mapData[0].length();
        grid = new char[rows][cols];
        totalOpenCells = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = mapData[r].charAt(c);
                if (grid[r][c] == '.') totalOpenCells++;
            }
        }

        // reset all path data
        bestLength = 0;
        bestPath.clear();
        currentPath.clear();
        visited = new boolean[rows][cols];
        
        // update ui labels safely
        if (bestLengthLabel != null) bestLengthLabel.setText("🏆 Best Route Found: 0");
        if (currentLengthLabel != null) currentLengthLabel.setText("🤖 Current Search Depth: 0");
        if (mazePanel != null) mazePanel.repaint();
    }

    private void toggleSimulation(ActionEvent e) {
        if (isRunning) {
            stopSimulation();
        } else {
            startSimulation();
        }
    }

    private void startSimulation() {
        startBtn.setText("⏹ Stop Simulation");
        startBtn.setBackground(new Color(231, 76, 60)); // red
        isRunning = true;
        bestLength = 0;
        bestPath.clear();
        
        solverThread = new Thread(() -> {
            try {
                // run dfs from every possible open cell
                for (int r = 0; r < rows && isRunning; r++) {
                    for (int c = 0; c < cols && isRunning; c++) {
                        if (grid[r][c] == '.') {
                            visited = new boolean[rows][cols];
                            currentPath.clear();
                            
                            visited[r][c] = true;
                            currentPath.add(new int[]{r, c});
                            
                            dfs(r, c, 1, totalOpenCells - 1);
                        }
                    }
                }
                
                SwingUtilities.invokeLater(() -> {
                    startBtn.setText("▶ Start Simulation");
                    startBtn.setBackground(new Color(46, 204, 113));
                    currentLengthLabel.setText("✅ Search Complete!");
                    currentPath.clear();
                    mazePanel.repaint();
                });
                isRunning = false;
                
            } catch (InterruptedException ex) {
                // thread interrupted
            }
        });
        solverThread.start();
    }

    private void stopSimulation() {
        isRunning = false;
        if (solverThread != null) {
            solverThread.interrupt();
        }
        startBtn.setText("▶ Start Simulation");
        startBtn.setBackground(new Color(46, 204, 113));
        currentPath.clear();
        mazePanel.repaint();
    }

    // --- The Core Backtracking Algorithm (Modified for GUI) ---
    private void dfs(int r, int c, int length, int remainingUnvisited) throws InterruptedException {
        if (!isRunning) return;

        // Update UI metrics safely
        SwingUtilities.invokeLater(() -> {
            currentLengthLabel.setText("🤖 Current Search Depth: " + length);
            if (length > bestLength) {
                bestLength = length;
                bestPath = new ArrayList<>(currentPath);
                bestLengthLabel.setText("🏆 Best Route Found: " + bestLength);
            }
            mazePanel.repaint();
        });

        // Sleep to animate the algorithm visually
        if (animationDelay > 0) {
            Thread.sleep(animationDelay);
        }

        // PRUNING LOGIC
        if (length + remainingUnvisited <= bestLength) {
            return;
        }

        for (int i = 0; i < 4; i++) {
            int nr = r + dRow[i];
            int nc = c + dCol[i];

            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && grid[nr][nc] == '.' && !visited[nr][nc]) {
                // Move forward
                visited[nr][nc] = true;
                currentPath.add(new int[]{nr, nc});
                
                dfs(nr, nc, length + 1, remainingUnvisited - 1);
                
                // Backtrack
                currentPath.remove(currentPath.size() - 1);
                visited[nr][nc] = false;
                
                // Animate backtracking
                SwingUtilities.invokeLater(mazePanel::repaint);
                if (animationDelay > 0) Thread.sleep(animationDelay / 2); // backtrack faster
            }
        }
    }

    // --- Custom Drawing Panel ---
    class MazePanel extends JPanel {
        public MazePanel() {
            setBackground(new Color(30, 30, 30)); // Dark background
        }

@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int pad = 40;
    int size = Math.min((getWidth() - (pad * 2)) / cols, (getHeight() - (pad * 2)) / rows);
    int startX = (getWidth() - (cols * size)) / 2;
    int startY = (getHeight() - (rows * size)) / 2;

    // 1. Draw the "Grid/Board"
    g2d.setColor(new Color(20, 20, 20)); // Deep black
    g2d.fillRect(startX, startY, cols * size, rows * size);
    
    g2d.setColor(new Color(40, 40, 40)); // Grid lines
    for (int i = 0; i <= cols; i++) g2d.drawLine(startX + i * size, startY, startX + i * size, startY + rows * size);
    for (int i = 0; i <= rows; i++) g2d.drawLine(startX, startY + i * size, startX + cols * size, startY + i * size);

    // 2. Draw Walls (Obstacles)
    for (int r = 0; r < rows; r++) {
        for (int c = 0; c < cols; c++) {
            if (grid[r][c] == '#') {
                g2d.setColor(new Color(80, 80, 80));
                g2d.fillRoundRect(startX + c * size + 4, startY + r * size + 4, size - 8, size - 8, 8, 8);
            }
        }
    }

    // 3. Draw the BEST PATH (The "Winning Snake")
    if (!bestPath.isEmpty()) {
        g2d.setColor(new Color(0, 255, 100, 150)); // Glowing Green
        g2d.setStroke(new BasicStroke(size * 0.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawPathLine(g2d, bestPath, startX, startY, size);
    }

    // 4. Draw CURRENT SEARCH (The "AI Thinking")
    if (!currentPath.isEmpty()) {
        g2d.setColor(new Color(0, 191, 255)); // Electric Blue
        g2d.setStroke(new BasicStroke(size * 0.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawPathLine(g2d, currentPath, startX, startY, size);

        // Draw Snake Head
        int[] head = currentPath.get(currentPath.size() - 1);
        g2d.setColor(Color.WHITE);
        g2d.fillOval(startX + head[1] * size + size/4, startY + head[0] * size + size/4, size/2, size/2);
    }
}

        private void drawPathLine(Graphics2D g2d, List<int[]> path, int startX, int startY, int size) {
            for (int i = 0; i < path.size() - 1; i++) {
                int[] p1 = path.get(i);
                int[] p2 = path.get(i + 1);
                
                int x1 = startX + p1[1] * size + size / 2;
                int y1 = startY + p1[0] * size + size / 2;
                int x2 = startX + p2[1] * size + size / 2;
                int y2 = startY + p2[0] * size + size / 2;
                
                g2d.drawLine(x1, y1, x2, y2);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LongestPathGUI gui = new LongestPathGUI();
            gui.setVisible(true);
        });
    }
}   