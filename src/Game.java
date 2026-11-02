import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class Game {
    private ArrayList<Level> levels;
    private Map map;
    private int width;
    private int height;
    private int fps = 16;

    int[][] pipes;
    int[][] obstacles0;
    int[][] keys0;
    int[][] locks0;
    int[][] coins0;

    Color skyColor = new Color(146, 144, 255);
    Color mainMenuColor0 = new Color(153, 78, 0);
    Color mainMenuColor1 = new Color(100, 50, 0);
    Color mainMenuColor2 = new Color(255, 204, 197);
    Color marioDarkBlue = new Color(32, 56, 136);
    Color marioRed = new Color(228, 0, 18);
    Color marioGold = new Color(255, 183, 0);
    Color marioLightBlue = new Color(107, 140, 255);

    public Game(ArrayList<Level> levels, Map map) {
        this.levels = levels;
        this.map = map;
        this.width = 800;
        this.height = 800;
        pipes = map.getPipes();
        obstacles0 = copy2DArray(map.getObstacles());
        coins0 = copy2DArray(this.levels.get(2).getCoins());
        keys0 = copy2DArray(this.levels.get(3).getKeys());
        locks0 = copy2DArray(this.levels.get(3).getLocks());

    }


    public void run() {

        int startX = pipes[0][0];
        int startY = pipes[0][1];

        Mario mario = new Mario(startX, startY);

        startScreen(mario);

        long gameStartTime = System.currentTimeMillis();
        int currentLevelIndex = 0;
        boolean isCameraFollowing = false;

        while (currentLevelIndex < levels.size()) {
            Level currentLevel = levels.get(currentLevelIndex);

            int[][] currentCoins;
            if (currentLevelIndex == 2 || currentLevelIndex == 3) {
                // take new copy for level 3 and 4
                currentCoins = copy2DArray(coins0);
            } else {
                // empty array if level has no coins
                currentCoins = new int[0][0];
            }

            if (currentLevelIndex == 3) {
                map.updateObstacles(currentLevel.getObstacles());
                map.updateKeys(copy2DArray(keys0));
                map.updateLocks(copy2DArray(locks0));
            } else {
                // clear map when restart pressed or at 1. 2. 3. levels
                map.updateObstacles(obstacles0);
                map.updateKeys(new int[0][0]);
                map.updateLocks(new int[0][0]);
            }
            map.hasKey = false; //for erasing key icon after restart

            mario.respawn();
            mario.x = startX;
            mario.y = startY;
            mario.speedY =-10;
            boolean levelComplete = false;
            int frameCount = 0;
            boolean cFlag = true;
            double camX = this.width/2.0;
            double camY = this.height/2.0;

            while (!levelComplete) {
                frameCount++;

                if (StdDraw.isKeyPressed(KeyEvent.VK_R)) {
                    currentLevelIndex = 0;
                    gameStartTime = System.currentTimeMillis();
                    break;
                }


                if (StdDraw.isKeyPressed(KeyEvent.VK_C) && cFlag) {
                    isCameraFollowing = !isCameraFollowing;
                    cFlag = false;
                }

                if (!StdDraw.isKeyPressed(KeyEvent.VK_C)) {
                    cFlag = true;
                }

                StdDraw.clear(this.skyColor);

                if (isCameraFollowing) {
                    if(mario.x <= 200){
                        camX = 200;
                    }
                    else if(this.width - mario.x <= 200){
                        camX = this.width - 200;
                    }
                    else{
                        camX = mario.x;
                    }
                    if(mario.y <= 200){
                        camY = 200;
                    }
                    else if(this.width - mario.y <= 200){
                        camY = this.width - 200;
                    }
                    else{
                        camY = mario.y;
                    }
                    StdDraw.setXscale(camX - 200, camX + 200);
                    StdDraw.setYscale(camY - 200, camY + 200);
                }
                else {
                    StdDraw.setXscale(0, this.width);
                    StdDraw.setYscale(0, this.height);
                }

                map.drawObstacles();
                map.setCoins(currentCoins);
                map.drawCoins();
                map.drawLocks();
                map.drawKeys();

                for (Enemy enemy : currentLevel.getEnemies()) {
                    enemy.move();
                    enemy.draw();

                    if (mario.checkEnemyCollision(enemy)) {
                        mario.die();

                        while (mario.y > -50) {
                            StdDraw.clear(this.skyColor);
                            map.drawObstacles();
                            map.drawPipes();
                            mario.y += (int) Math.floor(mario.speedY);
                            mario.speedY -= mario.gravity;

                            mario.draw();
                            map.drawPipes();
                            map.drawPortals();

                            StdDraw.show();
                            StdDraw.pause(1000 / fps);
                        }

                        mario.respawn();
                        break;
                    }
                }

                boolean onGround = map.isOnGround(mario.x, mario.y, mario.getSize()/2.0, mario.speedY, width, height);
                mario.handleInput(frameCount, onGround);
                mario.x = map.dontTouchSideOfObstacleOrWorldFrame(mario.x, mario.y, mario.getSize()/2.0, width);

                applyGravity(mario);

                map.collectCoin(mario);
                boolean sPressed = StdDraw.isKeyPressed(KeyEvent.VK_S);

                map.handleTeleport(mario, sPressed, mario.speedY);

                mario.draw();
                map.drawPipes();
                map.drawPortals();

                drawHUD(currentLevel, mario, gameStartTime, isCameraFollowing, camX, camY);

                if (map.isAtExit(mario)) {
                    if (currentLevel.getId() == 3 || currentLevel.getId() == 4) {
                        if (map.allCoinsCollected()) {
                            levelComplete = true;
                        }
                    }
                    else {
                        levelComplete = true;
                    }
                }

                StdDraw.show();
                StdDraw.pause(1000 / fps);
            }

            if (levelComplete) {
                String elapsedTime = getTime(gameStartTime);
                long pausedTime = levelBanner(currentLevel.getId(), currentLevel, currentLevel.getEnemies(), mario, elapsedTime, isCameraFollowing, camX, camY);
                gameStartTime += pausedTime;
                currentLevelIndex++;
            }
        }

        String finalTime = getTime(gameStartTime);
        int endChoice = endScreen(finalTime, mario.getDeathCount());

        if (endChoice == 1) {
            this.run();
        }
        else {
            System.exit(0);
        }
    }

    private void applyGravity(Mario mario) {
        boolean onGround = map.isOnGround(mario.x, mario.y, mario.getSize() / 2.0, mario.speedY, width, height);

        if (!onGround) {
            mario.speedY -= mario.gravity;
        }

        if (mario.speedY > 0) {
            mario.y += (int) Math.ceil(mario.speedY);
        } else {
            mario.y += (int) Math.floor(mario.speedY);
        }

        if (map.isOnGround(mario.x, mario.y, mario.getSize() / 2.0, mario.speedY, width, height)) {
            mario.y = map.correctedY;
            mario.speedY = 0;
        }
        else if (map.isOnCeiling(mario.x, mario.y, mario.getSize() / 2.0, mario.speedY, width, height)) {
            mario.y = map.correctedY;
            mario.speedY = -1.0;
        }
    }

    private void startScreen(Mario mario) {
        boolean startGame = false;
        int[][] obstacles = this.map.getObstacles();
        int y0 = 120 + (mario.getSize()/2);
        mario.x = (int)(width/2.0);
        mario.y = y0;
        int frameCount = 0;

        while (!startGame) {
            frameCount++;
            StdDraw.clear(skyColor);

            if (obstacles != null) {
                for(int i = 0; i < 60; i++) {
                    int x = obstacles[i][0];
                    int y = obstacles[i][1];
                    int halfWidth = obstacles[i][2];
                    int halfHeight = obstacles[i][3];

                    StdDraw.picture(x, y, "assets/block.png", halfWidth * 2, halfHeight * 2);
                }
            }

            double rectCenterX = width / 2.0;
            double rectCenterY = height * 0.7;
            double rectHalfW = 280;
            double rectHalfH = 100;

            if (StdDraw.isKeyPressed(KeyEvent.VK_LEFT) && fps > 10) fps -= 1;
            if (StdDraw.isKeyPressed(KeyEvent.VK_RIGHT) && fps < 60) fps += 1;

            StdDraw.setPenColor(mainMenuColor1);
            StdDraw.filledRectangle(rectCenterX, rectCenterY, rectHalfW + 5, rectHalfH + 5);

            StdDraw.setPenColor(mainMenuColor0);
            StdDraw.filledRectangle(rectCenterX, rectCenterY, rectHalfW, rectHalfH);

            StdDraw.setPenColor(mainMenuColor2);
            StdDraw.setFont(new Font("Monospaced", Font.BOLD, 42));
            StdDraw.text(rectCenterX, rectCenterY + 35, "SUPER");

            StdDraw.setFont(new Font("Monospaced", Font.BOLD, 65));
            StdDraw.text(rectCenterX, rectCenterY - 25, "MARIO BROS.");

            StdDraw.setPenColor(StdDraw.WHITE);
            StdDraw.setFont(new Font("Monospaced", Font.BOLD, 22));

            double infoY = height * 0.45;

            StdDraw.text(width / 2.0, infoY, "Press [Space] to start");

            StdDraw.setFont(new Font("Monospaced", Font.PLAIN, 20));
            StdDraw.text(width / 2.0, infoY - 40, "Move: [A] [D] [W]");
            StdDraw.text(width / 2.0, infoY - 80, "FPS: ~" + fps + "    Adjust: [←] [→]");

            boolean onGround = mario.y == y0;
            mario.handleInput(frameCount, onGround);

            int marioHalf = mario.getSize() / 2;
            if (mario.x < marioHalf) {
                mario.x = marioHalf;
            }
            if (mario.x > width - marioHalf) {
                mario.x = width - marioHalf;
            }

            if (!onGround) {
                mario.speedY -= mario.gravity;
            }

            if (mario.speedY > 0) {
                mario.y += (int) Math.ceil(mario.speedY);
            } else {
                mario.y += (int) Math.floor(mario.speedY);
            }

            if (mario.y <= y0) {
                mario.y = y0;
                if (mario.speedY < 0) {
                    mario.speedY = 0;
                }
            }

            mario.draw();

            if (StdDraw.isKeyPressed(KeyEvent.VK_SPACE)) {
                startGame = true;
            }

            StdDraw.show();
            StdDraw.pause(1000 / fps);
        }
    }

    private int endScreen(String elapsedTime, int deathCount) {
        StdDraw.setXscale(0, width);
        StdDraw.setYscale(0, height);
        int currentSelection = 1;
        boolean selectionMade = false;

        while (!selectionMade) {
            StdDraw.clear(marioDarkBlue);

            StdDraw.setPenColor(marioGold);
            StdDraw.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 75));
            StdDraw.text(width / 2.0, height * 0.8, "You Won!");

            StdDraw.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 36));

            if (currentSelection == 0) {
                StdDraw.setPenColor(marioGold);
                StdDraw.text(width / 2.0, height * 0.5, "> Exit <");
            } else {
                StdDraw.setPenColor(marioLightBlue);
                StdDraw.text(width / 2.0, height * 0.5, "Exit");
            }

            if (currentSelection == 1) {
                StdDraw.setPenColor(marioGold);
                StdDraw.text(width / 2.0, height * 0.4, "> Restart <");
            } else {
                StdDraw.setPenColor(marioLightBlue);
                StdDraw.text(width / 2.0, height * 0.4, "Restart");
            }

            StdDraw.setPenColor(StdDraw.WHITE);
            StdDraw.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18));
            StdDraw.text(width / 2.0, height * 0.22, "Time: " + elapsedTime);
            StdDraw.text(width / 2.0, height * 0.17, "Deaths: " + deathCount);

            if (StdDraw.isKeyPressed(KeyEvent.VK_W) || StdDraw.isKeyPressed(KeyEvent.VK_UP)) {
                currentSelection = 0;
                StdDraw.pause(150);
            }
            if (StdDraw.isKeyPressed(KeyEvent.VK_S) || StdDraw.isKeyPressed(KeyEvent.VK_DOWN)) {
                currentSelection = 1;
                StdDraw.pause(150);
            }

            if (StdDraw.isKeyPressed(KeyEvent.VK_ENTER) || StdDraw.isKeyPressed(KeyEvent.VK_SPACE)) {
                selectionMade = true;
                StdDraw.pause(200);
            }

            StdDraw.show();
            StdDraw.pause(1000 / fps);
        }

        return currentSelection;
    }

    private String getTime(long startingTime) {
        long elapsedTime = System.currentTimeMillis() - startingTime;
        long minutes = elapsedTime / 60000;
        long seconds = (elapsedTime / 1000) % 60;
        long centiseconds = (elapsedTime % 1000) / 10;
        return String.format("%02d:%02d:%02d", minutes, seconds, centiseconds);
    }

    private void drawHUD(Level currentLevel, Mario mario, long gameStartTime, boolean isCameraFollowing, double camX, double camY) {
        double centerX = isCameraFollowing ? camX : width / 2.0;
        double bottomY = isCameraFollowing ? camY - 200 : 0;
        int c = isCameraFollowing ? 2 : 1;


        StdDraw.setPenColor(marioDarkBlue);
        StdDraw.filledRectangle(centerX, bottomY + (60/c), 400/c, 60/c);

        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(new Font("SansSerif", Font.BOLD, 14));
        StdDraw.textLeft(centerX - (380/c), bottomY + (90/c), "Move: [A] [D] [W]");
        StdDraw.textLeft(centerX - (380/c), bottomY + (60/c), "Restart: [R]");
        StdDraw.textLeft(centerX - (380/c), bottomY + (30/c), "Camera: [C]");

        StdDraw.setPenColor(marioGold);
        StdDraw.setFont(new Font("SansSerif", Font.BOLD, 16));
        StdDraw.text(centerX, bottomY + (60/c), currentLevel.getClue());
        if (map.hasKey){
            StdDraw.setFont(new Font("Monospaced", Font.BOLD, 36));
            StdDraw.text(centerX, bottomY + (96/c), "🗝️"); //player sees key icon on HUD if they have a key
        }

        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(new Font("SansSerif", Font.BOLD, 14));
        StdDraw.textRight(centerX + (380/c), bottomY + (90/c), "Level " + currentLevel.getId());
        StdDraw.textRight(centerX + (380/c), bottomY + (60/c), getTime(gameStartTime));
        StdDraw.textRight(centerX + (380/c), bottomY + (30/c), "Deaths: " + mario.getDeathCount());
    }

    private long levelBanner(int id, Level currentLevel, ArrayList<Enemy> currentEnemies, Mario mario, String elapsedTime, boolean isCameraFollowing, double camX, double camY) {
        long pauseStart = System.currentTimeMillis();
        boolean spacePressed = false;

        double centerX = isCameraFollowing ? camX : width / 2.0;
        double centerY = isCameraFollowing ? camY : height / 2.0;
        int c = isCameraFollowing ? 2 : 1;

        while (!spacePressed) {
            StdDraw.setPenColor(marioDarkBlue);
            StdDraw.filledRectangle(centerX, centerY, 400/c, 100/c);

            StdDraw.setPenColor(marioGold);
            StdDraw.setFont(new Font("SansSerif", Font.BOLD, 45));
            StdDraw.text(centerX, centerY + 30/c, "Level " + id + " Complete!");

            StdDraw.setPenColor(StdDraw.WHITE);
            StdDraw.setFont(new Font("SansSerif", Font.BOLD, 20));
            StdDraw.text(centerX, centerY - 30/c, "Press [Space] to Continue");

            if (StdDraw.isKeyPressed(KeyEvent.VK_SPACE)) {
                spacePressed = true;
            }

            StdDraw.show();
            StdDraw.pause(1000 / fps);
        }

        StdDraw.pause(200);
        return System.currentTimeMillis() - pauseStart;
    }

    // !! to copy 2D arrays (deep copy:)
    private static int[][] copy2DArray(int[][] original) {
        if (original == null) return null;
        // construct new array (outside)
        int[][] copy = new int[original.length][];

        for (int i = 0; i < original.length; i++) {
            // clon every row (inside array)
            copy[i] = original[i].clone(); //.clone()
        }
        return copy;
    }

}
