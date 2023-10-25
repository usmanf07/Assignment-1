/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package rescuesimulation;

/**
 *
 * @author usman
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.io.File;

public class Rescuesimulation {

    private static final int TIMER_DELAY = 200;

    public static void main(String[] args) {
        int gridSize = 40;
        int cellSize = 25;

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Rescue Simulation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(gridSize * cellSize, gridSize * cellSize);
            Point ambulance = new Point(2, 3);
            Point accident = new Point(36, 37);

            GridPanel gridPanel = new GridPanel(gridSize, cellSize, ambulance, accident);
            frame.add(gridPanel);
            frame.setVisible(true);

            Timer timer = new Timer(TIMER_DELAY, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    gridPanel.moveAmbulance();
                    gridPanel.repaint();
                }
            });
            timer.start();
        });
    }
}

class GridPanel extends JPanel {
    private int gridSize;
    private int cellSize;
    private BufferedImage ambulanceImage;
    private BufferedImage accidentImage;
    private BufferedImage treeImage;
    private BufferedImage roadImage;
    private boolean[][] blockedCells;
    private Point ambulance;
    private Point accident;
    private List<Point> path;
    private Set<Point> visitedCells;
    private List<Point> shortestPath;
   private List<Point> ambulancePath;


    
    public GridPanel(int gridSize, int cellSize, Point ambulance, Point accident) {
        this.gridSize = gridSize;
        this.cellSize = cellSize;
        this.ambulance = ambulance;
        this.accident = accident;
        this.visitedCells = new HashSet<>();
        ambulancePath = new ArrayList<>();

        String ambulanceImagePath = "/rescuesimulation/icons/ambulance.png";
        String accidentImagePath = "/rescuesimulation/icons/accident.png";
        String treeImagePath = "/rescuesimulation/icons/tree.png";

        try {
            ambulanceImage = ImageIO.read(getClass().getResource(ambulanceImagePath));
            accidentImage = ImageIO.read(getClass().getResource(accidentImagePath));
            treeImage = ImageIO.read(getClass().getResource(treeImagePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        blockedCells = new boolean[gridSize][gridSize];
        generateBlockedCells();

         this.shortestPath = findShortestPath();
    }

    private void generateBlockedCells() {
        Random random = new Random();
        int numBlockedCells = gridSize * gridSize / 3;

        for (int i = 0; i < numBlockedCells; i++) {
            int x, y;
            do {
                x = random.nextInt(gridSize);
                y = random.nextInt(gridSize);
            } while ((x == ambulance.x && y == ambulance.y) || (x == accident.x && y == accident.y));
            blockedCells[x][y] = true;
        }
    }
    
     // A* Search algorithm to find the shortest path from ambulance to accident site
   private List<Point> findShortestPath() {
        List<Point> shortestPath = new ArrayList<>();
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getF));

        Node startNode = new Node(ambulance, null, 0, heuristic(ambulance, accident));
        openSet.add(startNode);

        Map<Point, Point> cameFrom = new HashMap<>();
        Map<Point, Double> gScore = new HashMap<>();
        gScore.put(ambulance, 0.0);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.getPosition().equals(accident)) {
                
                Point traceBack = accident;
                while (traceBack != null) {
                    shortestPath.add(traceBack);
                    traceBack = cameFrom.get(traceBack);
                }
                Collections.reverse(shortestPath);
                return shortestPath;
            }

            visitedCells.add(current.getPosition());

            for (Point neighbor : getNeighbors(current.getPosition())) {
                if (blockedCells[neighbor.x][neighbor.y]) {
                    continue;
                }

                double tentativeGScore = gScore.get(current.getPosition()) + 1;

                if (!gScore.containsKey(neighbor) || tentativeGScore < gScore.get(neighbor)) {
                    cameFrom.put(neighbor, current.getPosition());
                    gScore.put(neighbor, tentativeGScore);
                    double fScore = tentativeGScore + heuristic(neighbor, accident);
                    openSet.add(new Node(neighbor, current, tentativeGScore, fScore));
                }
            }
        }

        return null; // If no path is found, return null
    }
    
    private List<Point> getNeighbors(Point point) {
        List<Point> neighbors = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int newX = point.x + dx;
                int newY = point.y + dy;
                if (newX >= 0 && newX < gridSize && newY >= 0 && newY < gridSize && (dx != 0 || dy != 0)) {
                    neighbors.add(new Point(newX, newY));
                }
            }
        }
        return neighbors;
    }
    
    // Euclidean distance heuristic
    
    private double heuristic(Point from, Point to) {
        
        return Math.sqrt(Math.pow(from.x - to.x, 2) + Math.pow(from.y - to.y, 2));
    }

    public void moveAmbulance() {
        if (shortestPath == null) {
            
            JOptionPane.showMessageDialog(this, "No path found to the accident site.");
            System.exit(0);
            return; 
        }

        if (!shortestPath.isEmpty()) {
            Point nextPosition = shortestPath.remove(0);

            ambulancePath.add(ambulance);

            ambulance = nextPosition;

            if (ambulance.equals(accident)) {
                JOptionPane.showMessageDialog(this, "Ambulance reached the accident site!");
            }

            repaint();
        }
    }

    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                int xPos = x * cellSize;
                int yPos = y * cellSize;

                g.setColor(Color.BLACK);
                g.fillRect(xPos, yPos, cellSize, cellSize);

                if (ambulancePath.contains(new Point(x, y))) {
                    
                    g.setColor(Color.GREEN);
                    g.fillRect(xPos + 1, yPos + 1, cellSize - 2, cellSize - 2);
                }  else {
                    g.setColor(Color.WHITE);
                    g.fillRect(xPos + 1, yPos + 1, cellSize - 2, cellSize - 2);
                }

                if (x == ambulance.x && y == ambulance.y) {
                    g.drawImage(ambulanceImage, xPos, yPos, cellSize, cellSize, this);
                }

                if (x == accident.x && y == accident.y) {
                    g.drawImage(accidentImage, xPos, yPos, cellSize, cellSize, this);
                }

                if (blockedCells[x][y]) {
                    g.drawImage(treeImage, xPos, yPos, cellSize, cellSize, this);
                }
            }
        }
    }


    private class Node {
        private Point position;
        private Node parent;
        private double g;
        private double f;

        public Node(Point position, Node parent, double g, double f) {
            this.position = position;
            this.parent = parent;
            this.g = g;
            this.f = f;
        }

        public Point getPosition() {
            return position;
        }

        public double getF() {
            return f;
        }
    }
}