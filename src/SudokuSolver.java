import java.io.*;
import java.util.*;

public class SudokuSolver {
    public static void main(String[] args) throws IOException {
        int [][] board = readInput("input.txt");
        solveSudoku(board);
        writeOutput(board, "output.txt");
    }
    public static int[][] readInput(String filename) throws IOException {
        int[][] board = new int[9][9];
        BufferedReader br = new BufferedReader(new FileReader(filename));
            for (int i = 0; i < 9; i++) {
                String[] line = br.readLine().trim().split("\\s+"); for (int j = 0; j < 9; j++) {
                    board[i][j] = line[j].equals("-") ? 0 : Integer.parseInt(line[j]);
                }
    }
        br.close();
        return board;}
    public static void solveSudoku(int[][] board) {
        int populationSize = 10000;
        int generations = 1000;
        Set<String> mutablePositions = new HashSet<>();

        List<int[][]> population = initializePopulation(board, populationSize, mutablePositions);

        int bestFitness = Integer.MAX_VALUE;
        int[][] bestSolution = null;

        while (true) {
            for (int generation = 0; generation < generations; generation++) {
                population.sort(Comparator.comparingInt(SudokuSolver::fitness));
                int currentFitness = fitness(population.get(0));

                if (currentFitness < bestFitness) {
                    bestFitness = currentFitness;
                    bestSolution = deepCopy(population.get(0));
                }

                System.out.println("Generation: " + generation + ", Best Fitness: " + bestFitness);

                if (currentFitness == 0) {
                    copyBoard(population.get(0), board);
                    System.out.println("Solution found!");
                    return;
                }

                List<int[][]> newPopulation = new ArrayList<>();
                for (int i = 1; i < population.size(); i++) {
                    int[][] parent1 = population.get(i - 1);
                    int[][] parent2 = population.get(i);
                    int[][] child = crossover(parent1, parent2);
                    mutate(child, mutablePositions);
                    newPopulation.add(child);
                }

                population = newPopulation;
            }

            System.out.println("Restarting with best solution so far...");
            if (bestSolution != null) {
                population = initializePopulation(bestSolution, populationSize, mutablePositions);
            }
        }
    }


    private static List<int[][]> initializePopulation(int[][] board, int size, Set<String> mutablePositions) {
        List<int[][]> population = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int[][] individual = deepCopy(board);
            fillRandom(individual, mutablePositions);
            population.add(individual);
        }
        return population;
    }

    private static void fillRandom(int[][] board, Set<String> mutablePositions) {
        Random random = new Random();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] == 0) {
                    board[i][j] = random.nextInt(9) + 1;
                    mutablePositions.add(i + "," + j);
                }
            }
        }
    }

    private static int fitness(int[][] board) {
        int penalty = 0;
        for (int i = 0; i < 9; i++) {
            penalty += 9 - uniqueCount(board[i]); // Строки
            penalty += 9 - uniqueCount(getColumn(board, i)); // Столбцы
            penalty += 9 - uniqueCount(getBlock(board, i)); // Блоки 3x3
        }
        return penalty;
    }

    private static int uniqueCount(int[] array) {
        return (int) Arrays.stream(array).filter(x -> x != 0).distinct().count();
    }

    private static int[][] crossover(int[][] parent1, int[][] parent2) {
        int[][] child = deepCopy(parent1);
        Random random = new Random();
        int row = random.nextInt(9);
        System.arraycopy(parent2[row], 0, child[row], 0, 9);
        return child;
    }

    private static void mutate(int[][] board, Set<String> mutablePositions) {
        Random random = new Random();
        List<String> positions = new ArrayList<>(mutablePositions);
        int index = random.nextInt(positions.size());
        String[] pos = positions.get(index).split(",");
        int row = Integer.parseInt(pos[0]);
        int col = Integer.parseInt(pos[1]);

        int originalValue = board[row][col];
        int bestValue = originalValue;
        int bestFitness = fitness(board);

        for (int value = 1; value <= 9; value++) {
            if (value == originalValue) continue;
            board[row][col] = value;
            int currentFitness = fitness(board);
            if (currentFitness < bestFitness) {
                bestFitness = currentFitness;
                bestValue = value;
            }
        }

        board[row][col] = bestValue;
    }

    private static int[] getColumn(int[][] board, int colIndex) {
        int[] column = new int[9];
        for (int i = 0; i < 9; i++) {
            column[i] = board[i][colIndex];
        }
        return column;
    }

    private static int[] getBlock(int[][] board, int blockIndex) {
        int[] block = new int[9];
        int startRow = (blockIndex / 3) * 3;
        int startCol = (blockIndex % 3) * 3;

        int k = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                block[k++] = board[startRow + i][startCol + j];
            }
        }
        return block;
    }

    private static int[][] deepCopy(int[][] original) {
        int[][] copy = new int[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
        }
        return copy;
    }

    private static void copyBoard(int[][] source, int[][] destination) {
        for (int i = 0; i < source.length; i++) {
            System.arraycopy(source[i], 0, destination[i], 0, source[i].length);
        }
    }

    public static void writeOutput(int[][] board, String filename) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                bw.write(board[i][j] + (j < 8 ? " " : ""));
            }
            bw.newLine();
        }
        bw.close();
    }
}
