public class Bacterium {

    public int type;
    public float x;
    public float y;
    public boolean toBeDeleted;
    public int age;
    public float tx;
    public float ty;
    public float food;
    public int radius;
    public float speed = 0.5f;
    public float sightDistance = 100f;
    public float directionChangeRate = 0.01f;

    public Bacterium(int type, float x, float y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.toBeDeleted = false;
        this.age = 0;
        this.tx = 0;
        this.ty = 0;
        this.food = 5f;
        this.radius = 15;
    }
}