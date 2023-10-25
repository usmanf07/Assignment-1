/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rescuesimulation;

/**
 *
 * @author usman
 */
import java.awt.Point;
import java.util.*;

public class AStarPath {
    private int gridSize;
    private boolean[][] blockedCells;

    public AStarPath(int gridSize, boolean[][] blockedCells) {
        this.gridSize = gridSize;
        this.blockedCells = blockedCells;
    }

    public List<Point> findShortestPath(Point ambulance, Point accident) {
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

    private double heuristic(Point from, Point to) {
        return Math.sqrt(Math.pow(from.x - to.x, 2) + Math.pow(from.y - to.y, 2));
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

