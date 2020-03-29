import java.awt.*;

public class Food {
	
    public int type;
    public float x;
    public float y;
    public boolean toBeDeleted;

    public Food(float x, float y) {
        this.x = x;
        this.y = y;
        this.toBeDeleted = false;
    }
}