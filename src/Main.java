import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class Entity  {
    public float x;
    public float y;
    public float tx = 0;
    public float ty = 0;
    public float health = 1f;
    public float speed = 0.5f;
    public float force = 0.5f;
    public float intelligence = 0.5f;

    public Entity (float x, float y) {
        this.x = x;
        this.y = y;
    }
}

public class Main {
    final int W = 800;
    final int H = 800;
    float sumForce;
    float sumSpeed;
    float sumIntelligence;
    final float maxHealth = 3f;
    final float sightDistance = 100f;
    final float directionChangeRate = 0.01f;
    final float radiusEntity = 10f;
    final float chanceMutation = 0.0001f;
    final float coefHealthChange = 0.0001f;
    final float coefHealthIncrease = 0.002f;
    ArrayList<Entity> entities = new ArrayList<>();
    int countCycles = 0;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            }
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.add(new FormPane());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public class FormPane extends JPanel {
        public FormPane() {
            entities.add(new Entity((float) (Math.random() * (W)), (float) (Math.random() * (H))));
            Timer timer = new Timer(1, e -> repaint());
            timer.start();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(W, H);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            logic();
            draw(g);
        }

        private void draw(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 255));
            g2.fillRect(0, 0, W, H);
            g2.setColor(new Color(0, 0, 0, 255));
            g2.drawString("Count cycles: " + Float.toString(countCycles), 50, 250);
            g2.drawString("Count entities: " + Float.toString(entities.size()), 50, 200);
            g2.setColor(new Color(255, 0, 0, 255));
            g2.drawString("Average force: " + Float.toString(sumForce / entities.size()), 50, 150);
            g2.setColor(new Color(0, 255, 0, 255));
            g2.drawString("Average speed: " + Float.toString(sumSpeed / entities.size()), 50, 100);
            g2.setColor(new Color(0, 0, 255, 255));
            g2.drawString("Average intelligence: " + Float.toString(sumIntelligence / entities.size()), 50, 50);
            for (Entity a : entities) {
                g2.setColor(new Color(Math.round(a.force * 255), Math.round(a.speed * 255), Math.round(a.intelligence * 255), 130));
                g2.fillOval((int) (a.x - radiusEntity * Math.sqrt(a.health)), (int) (a.y - radiusEntity * Math.sqrt(a.health)), (int) (radiusEntity * Math.sqrt(a.health) * 2), (int) (radiusEntity * Math.sqrt(a.health)) * 2);
            }
        }

        private void logic() {
            countCycles++;
            sumForce = 0;
            sumSpeed = 0;
            sumIntelligence = 0;
            for (int i = 0; i < entities.size(); i++) {
                Entity a = entities.get(i);
                double targetAngle = Math.atan2(a.ty, a.tx);
                a.x += (float) Math.cos(targetAngle) * a.speed;
                a.y += (float) Math.sin(targetAngle) * a.speed;
                if (a.x < 0) a.x = W;
                else if (a.x > W) a.x = 0;
                if (a.y < 0) a.y = H;
                else if (a.y > H) a.y = 0;
                Entity closestFood = null;
                int indexClosestFood = 0;
                float minFoodDist = (W * W) + (H * H);
                for (int j = 0; j < entities.size(); j++) {
                    Entity b = entities.get(j);
                    if (b.force >= a.force) continue;
                    if (b.speed >= a.speed) continue;
                    float dist1 = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
                    float dist2 = (W - (a.x - b.x)) * (W - (a.x - b.x)) + (a.y - b.y) * (a.y - b.y);
                    float dist3 = (a.x - b.x) * (a.x - b.x) + (H - (a.y - b.y)) * (H - (a.y - b.y));
                    float dist4 = (W - (a.x - b.x)) * (W - (a.x - b.x)) + (H - (a.y - b.y)) * (H - (a.y - b.y));
                    float dist = Math.min(Math.min(dist1, dist2), Math.min(dist3, dist4));
                    if (dist < sightDistance * sightDistance) {
                        if (dist < minFoodDist) {
                            minFoodDist = dist;
                            closestFood = b;
                            indexClosestFood = j;
                        }
                    }
                }
                Entity closestEnemy = null;
                float minEnemyDist = (W * W) + (H * H);
                for (int j = 0; j < entities.size(); j++) {
                    Entity b = entities.get(j);
                    if (b.force <= a.force) continue;
                    if (b.speed <= a.speed) continue;
                    float dist1 = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
                    float dist2 = (W - (a.x - b.x)) * (W - (a.x - b.x)) + (a.y - b.y) * (a.y - b.y);
                    float dist3 = (a.x - b.x) * (a.x - b.x) + (H - (a.y - b.y)) * (H - (a.y - b.y));
                    float dist4 = (W - (a.x - b.x)) * (W - (a.x - b.x)) + (H - (a.y - b.y)) * (H - (a.y - b.y));
                    float dist = Math.min(Math.min(dist1, dist2), Math.min(dist3, dist4));
                    if (dist < sightDistance * sightDistance) {
                        if (dist < minEnemyDist) {
                            minEnemyDist = dist;
                            closestEnemy = b;
                        }
                    }
                }
                if (minFoodDist > minEnemyDist & closestEnemy != null) {
                    if (Math.abs(closestEnemy.x - a.x) > sightDistance)
                        a.tx = closestEnemy.x - a.x;
                    else
                        a.tx = -closestEnemy.x + a.x;
                    if (Math.abs(closestEnemy.y - a.y) > sightDistance)
                        a.ty = closestEnemy.y - a.y;
                    else
                        a.ty = -closestEnemy.y + a.y;
                } else {
                    if (closestFood != null) {
                        if (Math.abs(closestFood.x - a.x) > sightDistance)
                            a.tx = -closestFood.x + a.x;
                        else
                            a.tx = closestFood.x - a.x;
                        if (Math.abs(closestFood.y - a.y) > sightDistance)
                            a.ty = -closestFood.y + a.y;
                        else
                            a.ty = closestFood.y - a.y;
                        if (minFoodDist < radiusEntity * radiusEntity) {
                            if (closestFood.intelligence + closestFood.force > a.intelligence + a.force) {
                                closestFood.health *= 1 - ((a.intelligence + a.force) / (closestFood.intelligence + closestFood.force)) * ((a.intelligence + a.force) / (closestFood.intelligence + closestFood.force));
                                closestFood.health += a.health;
                                entities.remove(i);
                                i--;
                            } else {
                                a.health *= 1 - (closestFood.force / a.force) * (closestFood.force / a.force);
                                a.health += closestFood.health;
                                entities.remove(indexClosestFood);
                                if (indexClosestFood < i)
                                    i--;
                            }
                        }
                    } else {
                        if (Math.random() < directionChangeRate) {
                            double randomAngle = Math.random() * Math.PI * 2;
                            a.tx = (float) Math.cos(randomAngle) * 2;
                            a.ty = (float) Math.sin(randomAngle) * 2;
                        }
                    }
                }
                a.health += coefHealthIncrease;
                if (a.health >= maxHealth) {
                    a.health = 1f;
                    Entity b = new Entity(a.x + (float) Math.random() * 10 - 5, a.y + (float) Math.random() * 10 - 5);
                    b.speed = a.speed;
                    b.force = a.force;
                    b.intelligence = a.intelligence;
                    entities.add(b);
                }
                if (Math.random() < chanceMutation) {
                    a.speed = (float) (a.speed * (Math.random() / 5 + 0.9f));
                    if (a.speed >= 1)
                        a.speed = 0.999999f;
                    a.force = (float) (a.force * (Math.random() / 5 + 0.9f));
                    if (a.force >= 1)
                        a.force = 0.999999f;
                    a.intelligence = (float) (a.intelligence * (Math.random() / 5 + 0.9f));
                    if (a.intelligence >= 1)
                        a.intelligence = 0.999999f;
                }
                a.health -= (1 / (1 - a.speed)) * coefHealthChange;
                a.health -= (1 / (1 - a.intelligence)) * coefHealthChange;
                a.health -= (1 / (1 - a.force)) * coefHealthChange;

                if (a.health <= 0) {
                    entities.remove(i);
                    i--;
                }
                sumForce += a.force;
                sumSpeed += a.speed;
                sumIntelligence += a.intelligence;
            }
        }
    }
}