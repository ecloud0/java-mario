import java.awt.*;

public class Map {
    // '-' (private)
    private int[][] obstacles;
    private int[][] pipes;
    private int[][] portals;
    private int[][] coins;
    private int[][] keys = new int[0][4];
    private int[][] locks = new int[0][4];

    private int collisionIndex; // to keep track of which obstacle it hit
    public int correctedY;
    public int correctedX;
    private boolean teleported = false;
    public boolean hasKey = false;


    Color marioRed = new Color(228, 0, 18);
    Color marioGold = new Color(255, 183, 0);

    // Constructor
    public Map(int[][] obstacles, int[][] pipes, int[][] portals) {
        this.obstacles = obstacles;
        this.pipes = pipes;
        this.portals = portals;
    }

    public void updateObstacles(int[][] obstacles){
        this.obstacles = obstacles;
    }
    public void updateKeys(int[][] keys){
        this.keys = keys;
    }
    public void updateLocks(int[][] locks){
        this.locks = locks;
    }

    public int[][] getObstacles() { return this.obstacles; }
    public int[][] getPipes() { return this.pipes; }
    public int[][] getPortals() { return this.portals; }
    public int[][] getCoins() { return this.coins; }


    public boolean checkCollision(double nextX, double nextY, double entityHalfSize, int[][] elements) {
        for (int i = 0; i < elements.length; i++) {
            int elemX = elements[i][0];
            int elemY = elements[i][1];
            int elemHalfWidth = elements[i][2];
            int elemHalfHeight = elements[i][3];

            boolean collisionX = Math.abs(nextX - elemX) < (entityHalfSize + elemHalfWidth);
            boolean collisionY1 = nextY - elemY <= (entityHalfSize + elemHalfHeight);
            boolean collisionY2 = elemY - nextY <= (entityHalfSize + elemHalfHeight)-1;

            if (collisionX && collisionY1 && collisionY2) {
                this.collisionIndex = i; // tracks index for isOnGround and isOnCeiling
                return true;
            }
        }
        return false;
    }

    // ground control
    public boolean isOnGround(int x, int y, double halfSize, double speedY, int worldW, int worldH) {
        if (y - halfSize <= 0) {
            correctedY = (int)halfSize;
            return true;
        }


        if (speedY <= 0) {
            if (checkCollision(x, y, halfSize, this.obstacles)) {
                correctedY = obstacles[collisionIndex][1] + obstacles[collisionIndex][3] + (int)halfSize;
                return true;
            }

            if (checkCollision(x, y, halfSize, this.pipes)) {
                int pipeTop = pipes[collisionIndex][1] + pipes[collisionIndex][3];

                // Attention: it is accepted as ground if mario's center is above the pipetop
                if (y >= pipeTop) {
                    correctedY = pipeTop + (int)halfSize;
                    return true;
                }
                // if y < pipeTop, Mario is inside the pipe, onground turns false and mario falls.
            }
            if (checkCollision(x, y, halfSize, this.portals)) {
                int portalTop = portals[collisionIndex][1] + portals[collisionIndex][3];

                // Attention: it is accepted as ground if mario's center is above the portaltop
                if (y >= portalTop) {
                    correctedY = portalTop + (int)halfSize;
                    return true;
                }
            }
        }

        if (checkCollision(x, y, halfSize, this.keys)){
            keys[collisionIndex][0] = -100;
            hasKey = true;
        }

        return false;
    }

    // ceiling control
    public boolean isOnCeiling(int x, int y, double halfSize, double speedY, int worldW, int worldH) {
        correctedY = y;
        if (y + halfSize >= worldH) {
            correctedY = worldH - (int)halfSize;
            return true;
        }

        if (speedY > 0) {
            if (checkCollision(x, y, halfSize, this.obstacles)) {
                correctedY = obstacles[collisionIndex][1] - obstacles[collisionIndex][3] - (int)halfSize - 1;
                return true;
            }
            if (checkCollision(x, y, halfSize, this.pipes)) {
                if (collisionIndex < 2){
                correctedY = pipes[collisionIndex][1] - pipes[collisionIndex][3] - (int)halfSize - 1;
                return true;}
            }
        }
        return false;
    }


    public int dontTouchSideOfObstacleOrWorldFrame(int nextX, int y, double halfSize, int worldW) {
        correctedX = nextX;

        if (nextX - halfSize <= 0) correctedX = (int) halfSize;
        if (nextX + halfSize >= worldW) correctedX = worldW - (int) halfSize;



        for (int[] obs : obstacles) {
            boolean collisionX = Math.abs(correctedX - obs[0]) < (halfSize + obs[2]);
            boolean collisionY = Math.abs(y - obs[1]) < (halfSize + obs[3]);

            if (collisionX && collisionY) {
                if (correctedX < obs[0]) correctedX = obs[0] - obs[2] - (int)halfSize;
                else correctedX = obs[0] + obs[2] + (int)halfSize;
            }
        }
        for (int[] lock : locks) {
            boolean collisionX = Math.abs(correctedX - lock[0]) < (halfSize + lock[2]);
            boolean collisionY = Math.abs(y - lock[1]) < (halfSize + lock[3]);

            if (collisionX && collisionY) {
                if (hasKey) {
                    lock[0] = -100;
                    hasKey = false;
                    break;
                }

                else if (correctedX < lock[0]) correctedX = lock[0] - lock[2] - (int)halfSize;
                else correctedX = lock[0] + lock[2] + (int)halfSize;
            }
        }

        boolean hittingAnyPortal = false;
        for (int[] portal : portals) {
            boolean collisionX = Math.abs(correctedX - portal[0]) < (halfSize + portal[2]);
            boolean collisionY = Math.abs(y - portal[1]) < (halfSize + portal[3]);

            if (collisionX && collisionY) {
                hittingAnyPortal = true; // touches at least 1 portal
                if (!teleported) {
                    if (correctedX < portal[0]) correctedX = portal[0] - portal[2] - (int)halfSize;
                    else correctedX = portal[0] + portal[2] + (int)halfSize;
                }
            }
        }
        // if it doesn't touch any portals, reset the teleportation permission (to be able to use portals again)
        if (!hittingAnyPortal) {
            teleported = false;
        }

        return correctedX;
    }

    public void setCoins(int[][] coins) {
        this.coins = coins;
    }

    public int collectCoin(Mario mario) {
        int collectedCount = 0;
        double marioHalfSize = mario.getSize() / 2.0;

        for (int i = 0; i < coins.length; i++) {
            if (coins[i][0] == -100) continue;

            int[][] currentCoinArr = { coins[i] };

            if (checkCollision(mario.x, mario.y, marioHalfSize, currentCoinArr)) {
                coins[i][0] = -100;
                coins[i][1] = -100;
                collectedCount++;
            }
        }
        return collectedCount;
    }

    public boolean allCoinsCollected() {
        for (int i = 0; i < coins.length; i++) {
            if (coins[i][0] != -100) {
                return false;
            }
        }
        return true;
    }

    public void handleTeleport(Mario mario, boolean sPressed, double speedY) {
        double marioHalfSize = mario.getSize() / 2.0;

        int[][] upperPortalArr = { portals[0], portals[1] };
        int[][] lowerPortalArr = { portals[2], portals[3] };

        if (checkCollision(mario.x, mario.y, marioHalfSize, upperPortalArr) && speedY > 0) {
            mario.x = portals[2][0];
            mario.y = portals[2][1];
            mario.speedY = 15;
            teleported = true;
        } else if (checkCollision(mario.x, mario.y, marioHalfSize, lowerPortalArr) && sPressed) {
            mario.x = portals[0][0];
            mario.y = portals[0][1];
            mario.speedY = -10;
            teleported = true;
        }
    }

    // exit pipe
    public boolean isAtExit(Mario mario) {
        int[][] exitPipeArr = { pipes[2] };
        return checkCollision(mario.x, mario.y+1, mario.getSize() / 2.0, exitPipeArr);
    }

    // drawing methods
    public void drawObstacles() {
        for (int[] obstacle : obstacles) {
            StdDraw.picture(obstacle[0], obstacle[1], "assets/block.png", obstacle[2] * 2, obstacle[3] * 2);
        }
    }
    public void drawPipes() {
        StdDraw.setPenColor(marioGold);
        for (int[] pipe : pipes) {
            StdDraw.filledRectangle(pipe[0], pipe[1], pipe[2], pipe[3]);
        }
    }
    public void drawPortals() {
        StdDraw.setPenColor(marioRed);
        for (int[] portal : portals) {
            StdDraw.filledRectangle(portal[0], portal[1], portal[2], portal[3]);
        }
    }

    public void drawCoins() {
        for (int[] coin : coins) {
            if (coin[0] != -100) {
                StdDraw.picture(coin[0], coin[1], "assets/coin.png", 20, 20);
            }
        }
    }
    public void drawLocks() {
        for (int i = 0; i< locks.length ; i++) {
            String color = i == 0 ? "Yellow" : "Red";
            if (locks[i][0] != -100) {
                StdDraw.picture(locks[i][0], locks[i][1], "assets/lock" + color + ".png", locks[i][2]*2, locks[i][3]*2);
            }
        }
    }
    public void drawKeys() {
        for (int i = 0; i< locks.length ; i++) {
            String color = i == 0 ? "Yellow" : "Red";
            if (keys[i][0] != -100) {
                StdDraw.picture(keys[i][0], keys[i][1], "assets/key" + color + ".png", keys[i][2]*2, keys[i][3]*2);
            }
        }
    }
}