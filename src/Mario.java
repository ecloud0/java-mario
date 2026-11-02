import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class Mario {
    private final int spawnX = 60;
    private final int spawnY = 770;
    private final int size = 40;

    public int x;
    public int y;
    public double speedY;
    public double gravity = 2;

    private int deathCount;
    private boolean isDead;
    private boolean faceRight;
    private int state; // 0: standing, 1:walking, 2: jumping, 3: crouch
    private int currentFrame;

    // Constructor
    public Mario(int x, int y) {
        this.x = x;
        this.y = y;
        this.speedY = 0;
        this.deathCount = 0;
        this.isDead = false;
        this.faceRight = true;
        this.state = 0;
        this.currentFrame = 0;
    }

    // Getters
    public int getSize() { return size; }
    public int getDeathCount() { return deathCount; }
    public boolean getIsDead() { return isDead; }

    public int handleInput(int frame, boolean onGround) {
        if (isDead) return 0;

        this.currentFrame = frame;
        this.state = 0; // default state: standing

        // horizontal movement (A and D keys)
        if (StdDraw.isKeyPressed(KeyEvent.VK_A)) {
            this.x -= 10;
            this.faceRight = false;
            this.state = 1; // walking
        } else if (StdDraw.isKeyPressed(KeyEvent.VK_D)) {
            this.x += 10;
            this.faceRight = true;
            this.state = 1; // walking
        }

        // jumping (W - only when on ground)
        if (StdDraw.isKeyPressed(KeyEvent.VK_W) && onGround) {
            jump();
        }


        /*
        // !!! shortcut for quick debug
        if (StdDraw.isKeyPressed(KeyEvent.VK_Q)) {
            this.x = 100;
            this.y = 420;
        }
        */

        // crouch
        if (StdDraw.isKeyPressed(KeyEvent.VK_S)) {
            this.state = 3;
        }

        // onGround control
        if (!onGround) {
            this.state = 2; //jump animation
        }

        return this.state;
    }

    public void jump() {
        this.speedY = 25.0; // jumping boost
    }




    public boolean checkEnemyCollision(Enemy enemy) {
        if (isDead) return false;
        double marioHalf = this.size / 2.0;
        if ("mushroom".equals(enemy.getType())) {
            // enemy - mario collision control
            double enemyHalf = enemy.getSize() / 2.0;
            boolean collisionX = Math.abs(this.x - enemy.getX()) < (marioHalf + enemyHalf);
            boolean collisionY = Math.abs(this.y - enemy.getY()) < (marioHalf + enemyHalf);

            if (collisionX && collisionY) {
                return true;
            }
        }

        // enemy bullet - mario collision control
        if ("cannon".equals(enemy.getType())) {
            ArrayList<int[]> bullets = enemy.getBullets();

            // working backwards because we might delete an element from the list
            for (int i = bullets.size() - 1; i >= 0; i--) {
                int[] b = bullets.get(i);

                // b = {x, y, width, height} format
                double bulletHalfW = b[2] / 2.0;
                double bulletHalfH = b[3] / 2.0;

                boolean hitX = Math.abs(this.x - b[0]) < (marioHalf + bulletHalfW);
                boolean hitY = Math.abs(this.y - b[1]) < (marioHalf + bulletHalfH);

                if (hitX && hitY) {
                    bullets.remove(i);
                    return true;
                }
            }
        }
        return false;
    }



    public void die() {
        if (!isDead) {
            this.isDead = true;
            this.deathCount++;
            this.speedY = 15; // jump effect when die
        }
    }

    public void respawn() {
        this.x = spawnX;
        this.y = spawnY;
        this.isDead = false;
        this.speedY = 0;
        this.faceRight = true;
        this.state = 0;
    }

    public void draw() {
        String img;

        if (isDead) {
            img = "assets/dead.png";
        }
        else {
            switch (this.state) {
                case 1: // walking animation (every 4 frame img changes)
                    boolean step = (currentFrame / 2) % 2 == 0;
                    if (faceRight) {
                        img = step ? "assets/walkRight1.png" : "assets/walkRight2.png";
                    } else {
                        img = step ? "assets/walkLeft1.png" : "assets/walkLeft2.png";
                    }
                    break;

                case 2: // jump
                    img = faceRight ? "assets/jumpRight.png" : "assets/jumpLeft.png";
                    break;

                case 3: // crouch
                    img = "assets/crouch.png";
                    break;

                default: // stand
                    img = faceRight ? "assets/standRight.png" : "assets/standLeft.png";
                    break;
            }
        }

        StdDraw.picture(this.x, this.y, img, this.size, this.size);
    }
}