import java.util.ArrayList;

public class Level {
    // '-' (private)
    private int id;
    private ArrayList<Enemy> enemies;
    private String clue;
    private int[][] coins;
    private int[][] obstacles;
    private int[][] keys;
    private int[][] locks;


    public Level(int id, ArrayList<Enemy> enemies, String clue, int[][] coins) {
        this(id, enemies, clue, coins, new int[0][0], new int[0][0], new int[0][0]);
    }

    // Constructor
    public Level(int id, ArrayList<Enemy> enemies, String clue, int[][] coins, int[][] obstacles, int[][] keys, int[][] locks) {
        this.id = id;
        this.enemies = enemies;
        this.clue = clue;
        this.coins = coins;
        this.obstacles = obstacles;
        this.keys = keys;
        this.locks = locks;
    }

    public int getId() {
        return id;
    }

    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    public String getClue() {
        return clue;
    }

    public int[][] getCoins() {
        return coins;
    }

    public int[][] getObstacles() {
        return obstacles;
    }
    public int[][] getKeys() {
        return keys;
    }
    public int[][] getLocks() {
        return locks;
    }
    public void updateCoins(int[][] keys){
        this.coins = coins;
    }
    public void updateKeys(int[][] keys){
        this.keys = keys;
    }
    public void updateLocks(int[][] locks){
        this.locks = locks;
    }
}

