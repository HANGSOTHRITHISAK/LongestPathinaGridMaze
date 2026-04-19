import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnakeAI extends JFrame {

    private char[][] grid;
    private int rows, cols;
    private int totalOpenCells;
    private int bestLength = 0;
    private int currentScore = 0;
    
    private List<int[]> currentPath = new ArrayList<>();
    private List<int[]> bestPath = new ArrayList<>();
    private int[] foodCoord = new int[2];
    private boolean[][] visited;
    
    private Thread solverThread;
    private volatile boolean isRunning = false;
    private int animationDelay = 20; 

    private GamePanel gamePanel;
    private JLabel scoreLabel, statusLabel;
    private JSlider speedSlider;
    private JComboBox<String> levelSelector;
    private JButton startBtn;

    private final int[] dRow = {-1, 1, 0, 0};
    private final int[] dCol = {0, 0, -1, 1};

    public SnakeAI() {
        setTitle("Snake AI: NP-Hard Longest Path Solver");
        setSize(900, 950);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // header panel (scoreboard)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(10, 20, 10));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        scoreLabel = new JLabel("SCORE: 000");
        scoreLabel.setFont(new Font("Monospaced", Font.BOLD, 30));
        scoreLabel.setForeground(new Color(50, 255, 50));

        statusLabel = new JLabel("STATUS: IDLE");
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        statusLabel.setForeground(Color.WHITE);

        header.add(scoreLabel, BorderLayout.WEST);
        header.add(statusLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        gamePanel = new GamePanel();
        add(gamePanel, BorderLayout.CENTER);

        // control panel
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        controls.setBackground(new Color(30, 35, 30));

        String[] levels = {"Level: Small (4x4)", "Level: Medium (6x6)", "Level: Large (10x10)", "Level: 25x25 Complex"};
        levelSelector = new JComboBox<>(levels);
        levelSelector.addActionListener(e -> initLevel());

        startBtn = new JButton("START AI");
        startBtn.setFont(new Font("Arial", Font.BOLD, 14));
        startBtn.setBackground(new Color(0, 150, 0));
        startBtn.setForeground(Color.WHITE);
        startBtn.addActionListener(e -> toggleAI());

        speedSlider = new JSlider(0, 100, 20);
        speedSlider.addChangeListener(e -> animationDelay = speedSlider.getValue());
        
        JLabel sLab = new JLabel("SPEED:");
        sLab.setForeground(Color.WHITE);

        controls.add(levelSelector);
        controls.add(startBtn);
        controls.add(sLab);
        controls.add(speedSlider);
        add(controls, BorderLayout.SOUTH);

        initLevel();
    }

    private void initLevel() {
        stopAI();
        int idx = levelSelector.getSelectedIndex();
        String[] map;
        if (idx == 0) map = new String[]{"....",".###","....","...."};
        else if (idx == 1) map = new String[]{"......",".##...","..#.#.","...#..",".#....","......"};
        else if (idx == 2) map = new String[]{"..........","..........","..........","..........","..........","..........","..........","..........","..........",".........."};
        else map = new String[] {
            ".........................", ".#######################.", ".......................#.", ".#####################.#.", ".#.....................#.", ".#.#####################.", ".#.#.....................", ".#.#.###################.", ".#.#.#.................#.", ".#.#.#.###############.#.", ".#.#.#.#...............#.", ".#.#.#.#.#############.#.", ".#.#.#.#.#...........#.#.", ".#.#.#.#.#.#########.#.#.", ".#.#.#.#.#.#.......#.#.#.", ".#.#.#.#.#.#.#####.#.#.#.", ".#.#.#.#.#.#.#...#.#.#.#.", ".#.#.#.#.#.#.#.#.#.#.#.#.", ".#.#.#.#.#.#.#...#.#.#.#.", ".#.#.#.#.#.#.#####.#.#.#.", ".........................", "#######################..", ".........................", ".........................", "........................."
        };

        rows = map.length; cols = map[0].length();
        grid = new char[rows][cols];
        totalOpenCells = 0;
        for (int r=0; r<rows; r++) {
            for (int c=0; c<cols; c++) {
                grid[r][c] = map[r].charAt(c);
                if (grid[r][c] == '.') totalOpenCells++;
            }
        }
        
        spawnFood();
        bestLength = 0; currentScore = 0;
        bestPath.clear(); currentPath.clear();
        scoreLabel.setText("SCORE: 000");
        statusLabel.setText("STATUS: READY");
        gamePanel.repaint();
    }

    private void spawnFood() {
        Random rand = new Random();
        while(true) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            if (grid[r][c] == '.') {
                foodCoord[0] = r; foodCoord[1] = c;
                break;
            }
        }
    }

    private void toggleAI() {
        if (isRunning) stopAI(); else startAI();
    }

    private void startAI() {
        isRunning = true;
        startBtn.setText("STOP AI");
        startBtn.setBackground(new Color(200, 0, 0));
        statusLabel.setText("STATUS: THINKING...");
        
        solverThread = new Thread(() -> {
            try {
                // find a single valid start point (the snake's tail)
                int startR = -1, startC = -1;
                for(int r=0; r<rows; r++) {
                    for(int c=0; c<cols; c++) {
                        if(grid[r][c] == '.') { startR = r; startC = c; break; }
                    }
                    if(startR != -1) break;
                }

                visited = new boolean[rows][cols];
                currentPath.clear();
                visited[startR][startC] = true;
                currentPath.add(new int[]{startR, startC});

                // run backtracking from one point to avoid the "stuck" issue
                dfs(startR, startC, 1, totalOpenCells - 1);
                
                SwingUtilities.invokeLater(() -> statusLabel.setText("STATUS: FINISHED"));
            } catch (InterruptedException ex) {}
            isRunning = false;
        });
        solverThread.start();
    }

    private void stopAI() {
        isRunning = false;
        if (solverThread != null) solverThread.interrupt();
        startBtn.setText("START AI");
        startBtn.setBackground(new Color(0, 150, 0));
        currentPath.clear();
        gamePanel.repaint();
    }

    private void dfs(int r, int c, int length, int remaining) throws InterruptedException {
        if (!isRunning) return;

        // ui update
        SwingUtilities.invokeLater(() -> {
            if (length > bestLength) {
                bestLength = length;
                bestPath = new ArrayList<>(currentPath);
                currentScore = bestLength * 10;
                scoreLabel.setText(String.format("SCORE: %03d", currentScore));
            }
            gamePanel.repaint();
        });

        if (animationDelay > 0) Thread.sleep(animationDelay);

        // pruning
        if (length + remaining <= bestLength) return;

        for (int i=0; i<4; i++) {
            int nr = r + dRow[i], nc = c + dCol[i];
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && grid[nr][nc] == '.' && !visited[nr][nc]) {
                visited[nr][nc] = true;
                currentPath.add(new int[]{nr, nc});
                dfs(nr, nc, length + 1, remaining - 1);
                currentPath.remove(currentPath.size() - 1);
                visited[nr][nc] = false;
            }
        }
    }

    class GamePanel extends JPanel {
        public GamePanel() { setBackground(new Color(5, 10, 5)); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int pad = 60;
            int size = Math.min((getWidth() - pad*2)/cols, (getHeight() - pad*2)/rows);
            int sx = (getWidth() - cols*size)/2;
            int sy = (getHeight() - rows*size)/2;

            // draw board
            g2d.setColor(new Color(20, 30, 20));
            g2d.fillRect(sx, sy, cols*size, rows*size);

            // draw obstacles
            for (int r=0; r<rows; r++) {
                for (int c=0; c<cols; c++) {
                    if (grid[r][c] == '#') {
                        g2d.setColor(new Color(60, 70, 60));
                        g2d.fill3DRect(sx+c*size, sy+r*size, size, size, true);
                    }
                }
            }

            // draw the apple (food)
            g2d.setColor(new Color(255, 50, 50));
            g2d.fillOval(sx + foodCoord[1]*size + 4, sy + foodCoord[0]*size + 4, size - 8, size - 8);
            g2d.setColor(new Color(50, 255, 50)); // leaf
            g2d.fillRect(sx + foodCoord[1]*size + size/2, sy + foodCoord[0]*size + 2, 3, 5);

            // draw winning path (the full snake)
            if (!bestPath.isEmpty()) {
                g2d.setColor(new Color(0, 200, 0, 80));
                g2d.setStroke(new BasicStroke(size * 0.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                drawPath(g2d, bestPath, sx, sy, size);
            }

            // draw ai "thinking" snake
            if (!currentPath.isEmpty()) {
                g2d.setColor(new Color(100, 255, 100));
                g2d.setStroke(new BasicStroke(size * 0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                drawPath(g2d, currentPath, sx, sy, size);

                // Snake Head
                int[] head = currentPath.get(currentPath.size()-1);
                g2d.setColor(new Color(0, 255, 0));
                g2d.fillRoundRect(sx+head[1]*size+2, sy+head[0]*size+2, size-4, size-4, 10, 10);
                
                // Eyes
                g2d.setColor(Color.WHITE);
                g2d.fillOval(sx+head[1]*size + size/5, sy+head[0]*size + size/4, 6, 6);
                g2d.fillOval(sx+head[1]*size + 3*size/5, sy+head[0]*size + size/4, 6, 6);
                g2d.setColor(Color.BLACK);
                g2d.fillOval(sx+head[1]*size + size/5 + 2, sy+head[0]*size + size/4 + 1, 3, 3);
                g2d.fillOval(sx+head[1]*size + 3*size/5 + 2, sy+head[0]*size + size/4 + 1, 3, 3);
            }
        }

        private void drawPath(Graphics2D g2d, List<int[]> path, int sx, int sy, int sz) {
            for (int i=0; i<path.size()-1; i++) {
                int[] p1 = path.get(i), p2 = path.get(i+1);
                g2d.drawLine(sx+p1[1]*sz+sz/2, sy+p1[0]*sz+sz/2, sx+p2[1]*sz+sz/2, sy+p2[0]*sz+sz/2);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SnakeAI().setVisible(true));
    }
}