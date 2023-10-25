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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Rescuesimulation {

    private static final int TIMER_DELAY = 200;

    public static void main(String[] args) {
    int gridSize = 40;
    int cellSize = 20;

    SwingUtilities.invokeLater(() -> {
        JFrame frame = new JFrame("Rescue Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(gridSize * cellSize, gridSize * cellSize);
        Point ambulance = new Point(3, 2);
        Point accident = new Point(33, 34);

        GridPanel gridPanel = new GridPanel(gridSize, cellSize, ambulance, accident);
        frame.add(gridPanel);

        // Create a JButton for generating the A* Path
        JButton generateAStarPathButton = new JButton("Compare with A* Path");
        AStarPath aStarPath = new AStarPath(gridSize, gridPanel.getBlockedCells());
        List<Point> aStarShortestPath = aStarPath.findShortestPath(ambulance, accident);
        generateAStarPathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gridPanel.setaStarShortestPath(aStarShortestPath);
                gridPanel.repaint();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(generateAStarPathButton);
        frame.add(buttonPanel, BorderLayout.NORTH);

        frame.setVisible(true);

        // Create a background thread for running GA calculations
        SwingWorker<List<Point>, Void> gaWorker = new SwingWorker<List<Point>, Void>() {
            @Override
            protected List<Point> doInBackground() throws Exception {
                GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(10, 50, gridPanel, aStarShortestPath);
                List<Point> bestPath = geneticAlgorithm.findShortestPath();
                return bestPath;
            }

            @Override
            protected void done() {
                try {
                    List<Point> bestPath = get();
//                    int astarDistance = aStarShortestPath.size() - 1;
//                    displayDistance(astarDistance, "A Star");
//                    int GAbestPath = gridPanel.getBestIndividualPath().size() + 6;
//                    displayDistance(GAbestPath, "GA Best");
                    gridPanel.setBestPath(bestPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        gaWorker.execute(); // Start the GA calculations in the background

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

    
    private static void displayDistance(int distance, String name) {
        JOptionPane.showMessageDialog(null, name + " Distance: " + distance, "Distance", JOptionPane.INFORMATION_MESSAGE);
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
    private List<Point> aStarShortestPath;
   private List<Point> bestPath;
   private List<Point> bestIndividualPath;
    private boolean showBestPath = false;

    public List<Point> getShortestPath() {
        return shortestPath;
    }

    public List<Point> getAmbulancePath() {
        return ambulancePath;
    }

    public List<Point> getBestIndividualPath() {
        return bestIndividualPath;
    }

    public void setaStarShortestPath(List<Point> aStarShortestPath) {
        this.aStarShortestPath = aStarShortestPath;
    }

    public void setBestIndividualPath(List<Point> bestIndividualPath) {
        this.bestIndividualPath = bestIndividualPath;
    }
    
    
    public int getGridSize() {
        return gridSize;
    }
    
    public void showBestPath() {
        showBestPath = true;
        repaint();
    }
    
    public GridPanel(int gridSize, int cellSize, Point ambulance, Point accident) {
        this.gridSize = gridSize;
        this.cellSize = cellSize;
        this.ambulance = ambulance;
        this.accident = accident;
        this.visitedCells = new HashSet<>();
        ambulancePath = new ArrayList<>();
        bestPath = new ArrayList<>();
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
    
    void setShortestPath(List<Point> shortestPath)
    {
        this.shortestPath = shortestPath;
    }

    public void moveAmbulance() {
//        if (shortestPath.isEmpty()) {
//            //JOptionPane.showMessageDialog(this, "No path found to the accident site.");
//            System.exit(0);
//            return;
//        }

        if (bestPath != null && !bestPath.isEmpty()) {
            Point nextPosition = bestPath.remove(0);

            ambulancePath.add(ambulance);

            ambulance = nextPosition;

            if (ambulance.equals(accident)) {
                JOptionPane.showMessageDialog(this, "Ambulance reached the accident site!");
                
                bestPath = null;
            }
            
            repaint();
        }
    }
    
    void setBestPath(List<Point> bestPath) {
        this.bestPath = bestPath;
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
            
            if (showBestPath && bestPath.contains(new Point(x, y))) {
                g.setColor(Color.GREEN);
                g.fillRect(xPos + 1, yPos + 1, cellSize - 2, cellSize - 2);
            } else if (ambulancePath.contains(new Point(x, y))) {
                g.setColor(Color.GREEN);
                g.fillRect(xPos + 1, yPos + 1, cellSize - 2, cellSize - 2);
            } 
            else if (bestIndividualPath != null && bestIndividualPath.contains(new Point(x, y))) {
                g.setColor(Color.RED);
                //System.out.println("Red distance: " + bestIndividualPath.size());
                g.fillRect(xPos + 1, yPos + 1, cellSize - 2, cellSize - 2);
            }
            else if (aStarShortestPath != null && aStarShortestPath.contains(new Point(x, y))) {
                g.setColor(Color.YELLOW);
                
                g.fillRect(xPos + 1, yPos + 1, cellSize - 2, cellSize - 2);
            }
            else {
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
    Point getAmbulance() {
        return ambulance;
    }

    Point getAccident() {
        return accident;
    }

    boolean[][] getBlockedCells() {
        return blockedCells;
    }
}

class GeneticAlgorithm {
    private int populationSize;
    private int maxGenerations;
    private GridPanel gridPanel;
    private Point ambulance;
    private Point accident;
    private boolean[][] blockedCells;
    List<Point> aStarShortestPath;
    
    public GeneticAlgorithm(int populationSize, int maxGenerations, GridPanel gridPanel, List<Point> aStarShortestPath) {
        this.populationSize = populationSize;
        this.maxGenerations = maxGenerations;
        this.gridPanel = gridPanel;
        this.ambulance = gridPanel.getAmbulance();
        this.accident = gridPanel.getAccident();
        this.blockedCells = gridPanel.getBlockedCells();
        this.aStarShortestPath = aStarShortestPath;
    }
    
    
    public List<Point> findShortestPath() {
        double aStarDistance = calculatePathDistance(aStarShortestPath);
        System.out.println("a star Distance:" + aStarDistance);
        
        List<Individual> population = initializePopulation();
        evaluateFitness(population);
        System.out.println("Initial Population:");
        for (int i = 0; i < population.size(); i++) {
            Individual individual = population.get(i);
            List<Point> path = individual.getPath();
            System.out.printf("Individual %d: ", i + 1);

            System.out.printf("Fitness: %.2f%n", individual.getFitness());
        }

        List<Point> bestGene = new ArrayList<>();
        Individual bestIndividual = null;
        double closestFitness = 0;
        double threshold = 1.00;
       
        int generation = 0;

        while (closestFitness <= threshold) {       
            List<Individual> parents = selectParents(population,2);
            List<Individual> children = crossover(parents);
            mutate(children);
            evaluateFitness(children);
            population = selectNextGeneration(population, children);
            bestIndividual = getBestIndividual(population);
            gridPanel.setBestIndividualPath(bestIndividual.getFullPath());
            System.out.println("individualDistance: " + calculatePathDistance(bestIndividual.getFullPath()));
            System.out.println("astar: " + aStarDistance);
            closestFitness = bestIndividual.getFitness();
            generation++;
            
            System.out.printf("Generation %d - Closest Fitness: %.2f%n", generation, closestFitness);

//            for (int i = 0; i < population.size(); i++) {
//                Individual individual = population.get(i);
//                System.out.printf("Generation %d - Individual %d Fitness: %.2f%n", generation, i + 1, individual.getFitness());
//            }
           // break;
        }

        gridPanel.setShortestPath(bestIndividual.getFullPath());
        bestGene = bestIndividual.getFullPath();
        System.out.println("Best Gene:");
//        for (Point point : bestGene) {
//            System.out.printf("[%d, %d] ", point.x, point.y);
//        }
        System.out.printf("Fitness: %.2f%n", bestIndividual.getFitness());
        gridPanel.setBestPath(bestGene);

        return bestGene;
    }


    private List<Individual> initializePopulation() {
        List<Individual> population = new ArrayList<>();
        int populationSize = this.populationSize;

        while (population.size() < populationSize) {
            List<Point> path = generateRandomPath();
            List<Point> fullPath = generateFullPath(path);

            if (fullPath != null) {
                population.add(new Individual(path, fullPath));
            }
        }

        return population;
    }

    private List<Point> generateFullPath(List<Point> path) {
    List<Point> fullPath = new ArrayList<>();
    boolean isValidGene = true;
    Point prevPoint = null;

    for (int j = 0; j < path.size() - 1; j++) {
        List<Point> partialPath = calculateAStarPath(path.get(j), path.get(j + 1));
        if (partialPath == null) {
            isValidGene = false;
            break;
        }
        
        for (Point point : partialPath) {
            if (prevPoint == null || !prevPoint.equals(point)) {
                fullPath.add(point);
                prevPoint = point;
            }
        }
    }

    if (isValidGene) {
        // Add the last point in the path (accident)
        fullPath.add(path.get(path.size() - 1));
        return fullPath;
    } else {
        return null;
    }
}




    private List<Point> generateRandomPath() {
        List<Point> path = new ArrayList<>();

        path.add(ambulance);

        while (path.size() < 6) {
            Point randomPoint = generateRandomMove();
            path.add(randomPoint);
        }

        path.add(accident);

        return path;
    }

    private List<Point> calculateAStarPath(Point start, Point end) {
        AStarPath aStarPath = new AStarPath(gridPanel.getGridSize(), blockedCells);
        return aStarPath.findShortestPath(start, end);
    }

    private Individual selectParent(List<Individual> population) {
        // Calculate the total fitness of the population
        double totalFitness = 0;
        for (Individual individual : population) {
            totalFitness += individual.getFitness();
        }

        Random random = new Random();
        double spin = random.nextDouble() * totalFitness;

        double currentFitness = 0;
        for (Individual individual : population) {
            currentFitness += individual.getFitness();
            if (currentFitness >= spin) {
                return individual; // Selected parent
            }
        }

        // If we reach here, something went wrong, return a random individual as a fallback
        return population.get(random.nextInt(population.size()));
    }

    // Select a specified number of parents using roulette wheel selection
    private List<Individual> selectParents(List<Individual> population, int numParents) {
        List<Individual> parents = new ArrayList<>();
        for (int i = 0; i < numParents; i++) {
            Individual parent = selectParent(population);
            parents.add(parent);
        }
        return parents;
    }
    
    private List<Individual> crossover(List<Individual> parents) {
        
    List<Individual> children = new ArrayList<>();
    Random random = new Random();

    Individual parent1 = parents.get(0);
    Individual parent2 = parents.get(1);

    // Generate a random crossover point within the valid range
    int crossoverPoint = random.nextInt(Math.min(parent1.getPath().size(), parent2.getPath().size()));

    List<Point> childPath1 = new ArrayList<>(parent1.getPath().subList(0, crossoverPoint));
    childPath1.addAll(parent2.getPath().subList(crossoverPoint, parent2.getPath().size()));
    List<Point> childFullPath1 = generateFullPath(childPath1);

    List<Point> childPath2 = new ArrayList<>(parent2.getPath().subList(0, crossoverPoint));
    childPath2.addAll(parent1.getPath().subList(crossoverPoint, parent1.getPath().size()));
    List<Point> childFullPath2 = generateFullPath(childPath2);

    if (childPath1 != null && childPath2 != null) {
        children.add(new Individual(childPath1, childFullPath1));
        children.add(new Individual(childPath2, childFullPath2));
    }

    return children;
}




   private void mutate(List<Individual> children) {
        Random random = new Random();

        for (Individual child : children) {
            if (random.nextDouble() < mutationRate) {
                int mutateIndex = 1 + random.nextInt(child.getPath().size() - 2);
                Point mutatedPoint;

                do {
                    mutatedPoint = generateRandomMove();
                    child.getPath().set(mutateIndex, mutatedPoint);
                    child.setFullPath(generateFullPath(child.getPath()));
                } while (child.getFullPath() == null);
            }
        }
    }


    private Point generateRandomMove() {
        Random random = new Random();
        int gridSize = gridPanel.getGridSize();
        int randomX, randomY;
        Point randomPoint;

        int range = 5;
        randomX = random.nextInt(gridSize + range * 2) - range;
        randomY = random.nextInt(gridSize + range * 2) - range;

        randomX = Math.max(0, Math.min(randomX, gridSize - 1));
        randomY = Math.max(0, Math.min(randomY, gridSize - 1));

        randomPoint = new Point(randomX, randomY);

        while (blockedCells[randomX][randomY]) {
            randomX = random.nextInt(gridSize + range * 2) - range;
            randomY = random.nextInt(gridSize + range * 2) - range;
            randomX = Math.max(0, Math.min(randomX, gridSize - 1));
            randomY = Math.max(0, Math.min(randomY, gridSize - 1));
            randomPoint = new Point(randomX, randomY);
        }

        return randomPoint;
    }



    private void evaluateFitness(List<Individual> individuals) {
    //System.out.println("Fitness values:");
    for (int i = 0; i < individuals.size(); i++) {
        Individual individual = individuals.get(i);
        double fitness = calculateFitness(individual.getFullPath());
       
        individual.setFitness(fitness);
    }
}


    private double calculateFitness(List<Point> path) {
        double individualDistance = calculatePathDistance(path) - 1;
        double aStarDistance = calculatePathDistance(aStarShortestPath) + 1;
        
        double fitness = 1.0 / (individualDistance / aStarDistance);
        return fitness;
    }
    
    private double calculatePathDistance(List<Point> path) {
        return path.size() ; 
    }

   private List<Individual> selectNextGeneration(List<Individual> currentPopulation, List<Individual> children) {
    List<Individual> nextGeneration = new ArrayList<>(currentPopulation);
    currentPopulation.sort(Comparator.comparing(Individual::getFitness));
    nextGeneration.addAll(children);
    nextGeneration.sort(Comparator.comparing(Individual::getFitness));

    int worstCount = 2; 


//    // Print current generation
//    System.out.println("Current Generation:");
//    for (int i = 0; i < nextGeneration.size(); i++) {
//        Individual individual = nextGeneration.get(i);
//        System.out.printf("Individual %d - Fitness: %.2f%n", i + 1, individual.getFitness());
//    }
//    
      nextGeneration.subList(0, worstCount).clear();
//    // Print selected generation
//    System.out.println("Selected Generation:");
//    for (int i = 0; i < nextGeneration.size(); i++) {
//        Individual individual = nextGeneration.get(i);
//        System.out.printf("Individual %d - Fitness: %.2f%n", i + 1, individual.getFitness());
//    }
    
    
    return nextGeneration;
}




   private Individual getBestIndividual(List<Individual> population) {
    return Collections.max(population, Comparator.comparing(Individual::getFitness));
}


    private class Individual {
        private List<Point> path;
        private List<Point> fullPath;
        private double fitness;
        
        
        public Individual(List<Point> path, List<Point> fullPath) {
            this.path = path;
            this.fullPath = fullPath;
            this.fitness = 0.0;
        }

        public List<Point> getPath() {
            return path;
        }

        public List<Point> getFullPath() {
            return fullPath;
        }
        
        

        public double getFitness() {
            return fitness;
        }

        public void setFullPath(List<Point> fullPath) {
            this.fullPath = fullPath;
        }
        
        

        public void setFitness(double fitness) {
            this.fitness = fitness;
        }

        public boolean isGoalReached() {
            return !path.isEmpty() && path.get(path.size() - 1).equals(accident);
        }
    }

    private static final double mutationRate = 0.7; // Mutation rate (probability)
    
}
