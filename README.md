# Snake AI: NP-Hard Longest Path Solver
### An Autonomous Routing & Area Coverage Simulator

## 📖 Project Description
This project investigates the **Longest Simple Path problem** in a grid-based maze. While finding the shortest path between two points is computationally efficient (using BFS or Dijkstra's), determining the longest non-self-intersecting path is **NP-Hard**. 

Using the **Snake Game** as a visual metaphor, this application demonstrates how a backtracking algorithm with pruning can solve for "Perfect Play"—finding a Hamiltonian-style path that maximizes board coverage. This logic is critical for autonomous systems that require maximum area coverage without redundancy.

## 🚀 Features
*   **Backtracking Algorithm:** An exhaustive Depth-First Search (DFS) implementation to find the absolute maximum sequence of unique adjacent cells.
*   **Pruning Optimization:** Real-time upper-bound estimation (`current_length + remaining_cells <= best_length`) to skip billions of fruitless branches.
*   **Dynamic GUI:** Developed with Java Swing, featuring a live visualization of the "thinking" process (blue line) vs. the "best route" found (green line).
*   **Progressive Difficulty:** Multiple levels including a 4x4 starter, 11x11 maze challenge, and a 25x25 complex environment.

## 🛠 Real-World Applications
The "Longest Path" logic is vital in scenarios where "Efficiency" means "Maximum Coverage":
*   **Autonomous Area Coverage:** Routing logic for robotic vacuums (Roombas) or agricultural mowers to ensure 100% coverage in a single continuous pass.
*   **Drone Surveillance:** Maximizing unique observation points for security drones on a limited battery charge.
*   **Snake Game AI:** Solving the survival logic required to fill an entire game board without the snake trapping itself in a corner.

## 🔬 Complexity Analysis
As established in our research (Sipser, 2013), this problem is a general form of the **Hamiltonian Path problem**.
*   **Time Complexity:** Worst-case **O(3ⁿ)**. Because each step offers roughly 3 new directions, the search space grows exponentially with the number of open cells (*n*). 
*   **Space Complexity:** **O(n)**, governed by the depth of the recursion stack.
*   **Pruning Efficiency:** Walls and obstacles act as natural chokepoints that reduce the "branching factor," allowing the algorithm to solve complex 25x25 mazes significantly faster than open grids.

## 💻 How to Run
Ensure you have the **Java Development Kit (JDK)** installed.

1. **Navigate to the source folder:**
   ```bash
   cd src
2. **Compile the application:**
   ```bash
   javac SnakeAI.java
1. **Launch the Simulation:**
   ```bash
   java SnakeAI


## 📖 Project Description
Cormen, T. H., Leiserson, C. E., Rivest, R. L., & Stein, C. (2009). Introduction to Algorithms (3rd ed.). MIT Press.

Sipser, M. (2013). Introduction to the Theory of Computation (3rd ed.). Cengage Learning.
