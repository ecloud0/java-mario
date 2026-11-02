import java.util.ArrayList;

public class Enemy {
    private double x, y;
    private int size;
    private double x1, x2; // walking line start and end point
    private double speed;
    private int direction; // 1: right, -1: left, 0: down
    private String type;
    private ArrayList<int[]> bullets;
    private int shootPeriod = 0;


    // Short constructor
    public Enemy(double x, double y, int size, int direction, double x1, double x2, String type) {
        this(x, y, size, direction, x1, x2, type, new ArrayList<>());
    }

    // main constructor
    public Enemy(double x, double y, int size, int direction, double x1, double x2, String type, ArrayList<int[]> bullets) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.direction = direction;
        this.speed = 2.0;
        this.x1 = x1;
        this.x2 = x2;
        this.type = type;
        this.bullets = bullets;
        if (this.type.equals("cannon")) {
            bullets.add(new int[]{(int)this.x+2, (int)this.y+10, 30, 18});
        }

    }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public int getSize() { return size; }
    public String getType() { return type; }
    public ArrayList<int[]> getBullets(){ return bullets; }

    public void move() {

        if (this.type.equals("cannon")) {
            shootPeriod++;

            if (shootPeriod >= 60) {
                // {x, y, width, height} format
                bullets.add(new int[]{(int)this.x+2
                        , (int)this.y+10, 30, 18});
                shootPeriod = 0; // reset the counter
            }
            // working backwards because we might delete an element from the list
            for (int i = bullets.size() - 1; i >= 0; i--) {
                int[] b = bullets.get(i);

                if (direction == 0) {
                    b[1] -= 5; // bullet speed is 5, downwards
                } else {
                    b[0] += (direction * 5);
                }

                // erase the bullets coming out of range
                if (direction == 0) {
                    // if it shoots downwards x2 means lowest point of range
                    if (b[1] < x2) {
                        bullets.remove(i);
                    }
                } else {
                    // if it shoots right or left x1 x2 means range limits
                    if (b[0] < x1 || b[0] > x2) {
                        bullets.remove(i);
                    }
                }
            }
        }
        else if (this.type.equals("mushroom")){
            x += (speed * direction);
            if (x <= x1 || x >= x2) {
                direction *= -1; // turn back
        }

        }
    }

    public void draw() {
        String imagePath = "assets/" + type + ".png";
        int width = this.size;
        int height = this.size;
        if (this.type.equals("cannon")) {
            if(direction==0){
                imagePath = "assets/cannonDown.png";
                width = 42;
                height = 50;
                for(int[] b : bullets){
                    StdDraw.picture(b[0], b[1], "assets/bulletDown.png", b[3], b[2]);
                }
            }
            else if (direction > 0){
                imagePath = "assets/cannonRight.png";
                width = 50;
                height = 40;
                for(int[] b : bullets){
                    StdDraw.picture(b[0], b[1], "assets/bulletRight.png", b[2], b[3]);
                }
            }
            else {
                imagePath = "assets/cannonLeft.png";
                width = 48;
                height = 40;
                for(int[] b : bullets){
                    StdDraw.picture(b[0], b[1], "assets/bulletLeft.png", b[2], b[3]);
                }
            }
        }
        StdDraw.picture(x-2, y, imagePath, width, height);
    }

}