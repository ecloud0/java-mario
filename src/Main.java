import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        final int width = 800;
        final int height = 800;

        final int cameraModeW = 400;
        final int cameraModeH = 400;

        StdDraw.setCanvasSize(width, height);
        StdDraw.setXscale(0, width);
        StdDraw.setYscale(0, height);
        StdDraw.setTitle("Super Mario Bros.");
        StdDraw.enableDoubleBuffering();

        Enemy enemy0 = new Enemy(340, 580, 40, -1, 260, 420, "mushroom");
        Enemy enemy1 = new Enemy(440, 300, 40, 1, 380, 500, "mushroom");
        ArrayList<int[]> bulletsRight = new ArrayList<>();
        ArrayList<int[]> bulletsLeft = new ArrayList<>();
        ArrayList<int[]> bulletsDown = new ArrayList<>();
        Enemy cannon0 = new Enemy(140, 660, 40, 1, 140, 650, "cannon", bulletsRight);
        Enemy cannon1 = new Enemy(740, 620, 40, 0, 740, 280, "cannon", bulletsLeft);
        Enemy cannon2 = new Enemy(540, 420, 40, -1, 140, 540, "cannon", bulletsDown);


        String clue0 = "Go to the exit pipe!";
        String clue1 = "Do not mess with mushrooms!";
        String clue2 = "Do not forget to collect all the coins!";
        String clue3 = "Watch out for bullets and find a key to unlock!";


        int[][] obstacles = readFromFile("data/obstacles.txt");
        int[][] pipes = readFromFile("data/pipes.txt");
        int[][] portals = readFromFile("data/portals.txt");
        int[][] coins = readFromFile("data/coins.txt");
        int[][] extraBlocks = {{700, 660, 20, 20}, {660, 700, 20, 20}, {620, 740, 20, 20}, {620, 780, 20, 20}, {100, 500, 20, 20}, {100, 460, 20, 20}, {700, 620, 20, 20}};
        int[][] obstacles2 = readFromFile("data/obstacles.txt", extraBlocks);
        int[][] keys = {{270, 580, 20, 10}, {740, 700, 20, 10}};
        int[][] locks = {{700, 580, 20, 20}, {100, 420, 20, 20}};


        // constuct map object
        Map gameMap = new Map(obstacles, pipes, portals);

        // constructing level objects
        ArrayList<Level> levels = new ArrayList<>();

        // Level 1: no enemy, no coins
        levels.add(new Level(1, new ArrayList<>(), clue0, new int[0][0]));

        // Level 2: 2 enemies, no coin
        ArrayList<Enemy> enemiesLevel2 = new ArrayList<>();
        enemiesLevel2.add(enemy0);
        enemiesLevel2.add(enemy1);
        levels.add(new Level(2, enemiesLevel2, clue1, new int[0][0]));

        // Level 3: coins must be collected
        ArrayList<Enemy> enemiesLevel3 = enemiesLevel2;
        levels.add(new Level(3, enemiesLevel3, clue2, coins));

        // Level 4: custom
        ArrayList<Enemy> enemiesLevel4 = new ArrayList<>(enemiesLevel2);
        enemiesLevel4.add(cannon0);
        enemiesLevel4.add(cannon1);
        enemiesLevel4.add(cannon2);
        levels.add(new Level(4, enemiesLevel4, clue3, coins, obstacles2, keys, locks));

        // construct game
        Game game = new Game(levels, gameMap);

        // start game loop
        game.run();

        // Note: StdDraw.show() is in while loop in game.run()
    }

    public static int[][] readFromFile(String txtFile){
        return readFromFile(txtFile, null);
    }

    public static int[][] readFromFile(String txtFile, int[][] extra) {
        ArrayList<int[]> list = new ArrayList<>();
        try {
            File file = new File(txtFile);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                int[] row = new int[4];
                for (int i = 0; i < 4; i++) {
                    row[i] = Integer.parseInt(parts[i].trim());
                }
                list.add(row);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: Document couldn't found -> " + txtFile);
        }
        if (extra != null){
            for (int[] e : extra){
                list.add(e);
            }

        }
        // we are transforming dynamic arraylist to static int array
        int[][] result = new int[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }
}
