import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class Entity  {
    public float x;
    public float y;
    public float tx = 0;
    public float ty = 0;
    public float health = 1f;
    public float maxSpeed = 0.5f;
    public float currentSpeed = maxSpeed;
    public float force = 0.5f;
    public float agility = 0.5f;
    public float stamina = 0.5f;
    public boolean useStamina = false;
    public float predator = 0f;


    public Entity (float x, float y) {
        this.x = x;
        this.y = y;
    }
}

public class Main {
    final int W = 800;
    final int H = 800;
    float sumForce;
    float sumMaxSpeed;
    float sumAgility;
    float sumStamina;
    float sumPredator;
    final float maxHealth = 3f;
    final float sightDistance = 100f;
    final float directionChangeRate = 0.01f;
    final float radiusEntity = 10f;
    final float chanceMutation = 0.1f;
    final float coefHealthChange = 0.00015f;
    final float coefHealthIncrease = 0.001f;
    final float coefMutation = 0.1f;
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
            g2.drawString("Count cycles: " + Float.toString(countCycles), 50, 50);
            g2.drawString("Count entities: " + Float.toString(entities.size()), 50, 100);
            g2.setColor(new Color(255, 0, 0, 255));
            g2.drawString("Average force: " + Float.toString(sumForce / entities.size()), 50, 150);
            g2.setColor(new Color(0, 255, 0, 255));
            g2.drawString("Average max speed: " + Float.toString(sumMaxSpeed / entities.size()), 50, 200);
            g2.setColor(new Color(0, 0, 255, 255));
            g2.drawString("Average stamina: " + Float.toString(sumStamina / entities.size()), 50, 250);
            g2.setColor(new Color(0, 0, 0, 255));
            g2.drawString("Average agility: " + Float.toString(sumAgility / entities.size()), 50, 300);
            g2.drawString("Average predator: " + Float.toString(sumPredator / entities.size()), 50, 350);
            for (Entity a : entities) {
                if (a.predator == 1f) {
                    g2.setColor(new Color(0, 0, 0, 255));
                    g2.drawOval((int) (a.x - radiusEntity * Math.sqrt(a.health)), (int) (a.y - radiusEntity * Math.sqrt(a.health)), (int) (radiusEntity * Math.sqrt(a.health) * 2), (int) (radiusEntity * Math.sqrt(a.health)) * 2);
                }
                g2.setColor(new Color(Math.round(a.force * 255), Math.round(a.maxSpeed * 255), Math.round(a.stamina * 255), 130));
                g2.fillOval((int) (a.x - radiusEntity * Math.sqrt(a.health)), (int) (a.y - radiusEntity * Math.sqrt(a.health)), (int) (radiusEntity * Math.sqrt(a.health) * 2), (int) (radiusEntity * Math.sqrt(a.health)) * 2);
            }
        }

        private void logic() {
            countCycles++;
            sumForce = 0;
            sumMaxSpeed = 0;
            sumStamina = 0;
            sumAgility = 0;
            sumPredator = 0;
            for (int currentEntityIndex = 0; currentEntityIndex < entities.size(); currentEntityIndex++) {
                Entity currentEntity = entities.get(currentEntityIndex);
                double targetAngle = Math.atan2(currentEntity.ty, currentEntity.tx);

                if (currentEntity.useStamina) {
                    currentEntity.x += (float) Math.cos(targetAngle) * currentEntity.currentSpeed;
                    currentEntity.y += (float) Math.sin(targetAngle) * currentEntity.currentSpeed;
                    currentEntity.currentSpeed *= 1 - currentEntity.stamina / 1000;
                } else {
                    currentEntity.x += (float) Math.cos(targetAngle) * currentEntity.currentSpeed / 4;
                    currentEntity.y += (float) Math.sin(targetAngle) * currentEntity.currentSpeed / 4;
                    currentEntity.currentSpeed /= 1 - currentEntity.stamina / 1000;
                    if (currentEntity.currentSpeed > currentEntity.maxSpeed)
                        currentEntity.currentSpeed = currentEntity.maxSpeed;
                }
                if (currentEntity.x < 0) currentEntity.x = W;
                else if (currentEntity.x > W) currentEntity.x = 0;
                if (currentEntity.y < 0) currentEntity.y = H;
                else if (currentEntity.y > H) currentEntity.y = 0;
                Entity closestFood = null;
                int indexClosestFood = 0;
                float minFoodDist = (W * W) + (H * H);
                for (int i = 0; i < entities.size(); i++) {
                    Entity e = entities.get(i);
                    if (currentEntity.predator == 0f) continue;
                    if (e.force >= currentEntity.force) continue;
                    float dist1 = (currentEntity.x - e.x) * (currentEntity.x - e.x) + (currentEntity.y - e.y) * (currentEntity.y - e.y);
                    float dist2 = (W - Math.abs(currentEntity.x - e.x)) * (W - Math.abs(currentEntity.x - e.x)) + (currentEntity.y - e.y) * (currentEntity.y - e.y);
                    float dist3 = (currentEntity.x - e.x) * (currentEntity.x - e.x) + (H - Math.abs(currentEntity.y - e.y)) * (H - Math.abs(currentEntity.y - e.y));
                    float dist4 = (W - Math.abs(currentEntity.x - e.x)) * (W - Math.abs(currentEntity.x - e.x)) + (H - Math.abs(currentEntity.y - e.y)) * (H - Math.abs(currentEntity.y - e.y));
                    float dist = Math.min(Math.min(dist1, dist2), Math.min(dist3, dist4));
                    if (dist < sightDistance * sightDistance) {
                        if (dist < minFoodDist) {
                            minFoodDist = dist;
                            closestFood = e;
                            indexClosestFood = i;
                        }
                    }
                }
                Entity closestEnemy = null;
                float minEnemyDist = (W * W) + (H * H);
                for (int i = 0; i < entities.size(); i++) {
                    Entity e = entities.get(i);
                    if (e.predator == 0f) continue;
                    if (e.force <= currentEntity.force) continue;
                    float dist1 = (currentEntity.x - e.x) * (currentEntity.x - e.x) + (currentEntity.y - e.y) * (currentEntity.y - e.y);
                    float dist2 = (W - Math.abs(currentEntity.x - e.x)) * (W - Math.abs(currentEntity.x - e.x)) + (currentEntity.y - e.y) * (currentEntity.y - e.y);
                    float dist3 = (currentEntity.x - e.x) * (currentEntity.x - e.x) + (H - Math.abs(currentEntity.y - e.y)) * (H - Math.abs(currentEntity.y - e.y));
                    float dist4 = (W - Math.abs(currentEntity.x - e.x)) * (W - Math.abs(currentEntity.x - e.x)) + (H - Math.abs(currentEntity.y - e.y)) * (H - Math.abs(currentEntity.y - e.y));
                    float dist = Math.min(Math.min(dist1, dist2), Math.min(dist3, dist4));
                    if (dist < sightDistance * sightDistance) {
                        if (dist < minEnemyDist) {
                            minEnemyDist = dist;
                            closestEnemy = e;
                        }
                    }
                }
                if (minFoodDist > minEnemyDist & closestEnemy != null) {
                    currentEntity.useStamina = true;
                    if (Math.abs(closestEnemy.x - currentEntity.x) > sightDistance)
                        currentEntity.tx = closestEnemy.x - currentEntity.x;
                    else
                        currentEntity.tx = -closestEnemy.x + currentEntity.x;
                    if (Math.abs(closestEnemy.y - currentEntity.y) > sightDistance)
                        currentEntity.ty = closestEnemy.y - currentEntity.y;
                    else
                        currentEntity.ty = -closestEnemy.y + currentEntity.y;
                } else {
                    currentEntity.useStamina = true;
                    if (closestFood != null) {
                        if (Math.abs(closestFood.x - currentEntity.x) > sightDistance)
                            currentEntity.tx = -closestFood.x + currentEntity.x;
                        else
                            currentEntity.tx = closestFood.x - currentEntity.x;
                        if (Math.abs(closestFood.y - currentEntity.y) > sightDistance)
                            currentEntity.ty = -closestFood.y + currentEntity.y;
                        else
                            currentEntity.ty = closestFood.y - currentEntity.y;
                        if (minFoodDist < radiusEntity * radiusEntity) {
                            if (closestFood.agility + closestFood.force > currentEntity.agility + currentEntity.force) {
                                closestFood.health *= 1 - ((currentEntity.agility + currentEntity.force) / (closestFood.agility + closestFood.force)) * ((currentEntity.agility + currentEntity.force) / (closestFood.agility + closestFood.force));
                                closestFood.health += currentEntity.health;
                                entities.remove(currentEntityIndex);
                                currentEntityIndex--;
                            } else {
                                currentEntity.health *= 1 - (closestFood.force / currentEntity.force) * (closestFood.force / currentEntity.force);
                                currentEntity.health += closestFood.health;
                                entities.remove(indexClosestFood);
                                if (indexClosestFood < currentEntityIndex)
                                    currentEntityIndex--;
                            }
                        }
                    } else {
                        currentEntity.useStamina = false;
                        if (Math.random() < directionChangeRate) {
                            double randomAngle = Math.random() * Math.PI * 2;
                            currentEntity.tx = (float) Math.cos(randomAngle) * 2;
                            currentEntity.ty = (float) Math.sin(randomAngle) * 2;
                        }
                    }
                }
                currentEntity.health += coefHealthIncrease;
                if (currentEntity.health >= maxHealth) {
                    currentEntity.health = 1f;
                    Entity newEntity = new Entity(currentEntity.x + (float) Math.random() * 10 - 5, currentEntity.y + (float) Math.random() * 10 - 5);
                    newEntity.maxSpeed = currentEntity.maxSpeed;
                    newEntity.force = currentEntity.force;
                    newEntity.agility = currentEntity.agility;
                    newEntity.stamina = currentEntity.stamina;
                    newEntity.predator = currentEntity.predator;
                    if (Math.random() < chanceMutation) {
                        newEntity.health = 1f;
                        if (Math.random() < 0.5f)
                            newEntity.maxSpeed = newEntity.maxSpeed * (1 - coefMutation);
                        else
                            newEntity.maxSpeed = newEntity.maxSpeed + (1 - newEntity.maxSpeed) * coefMutation;
                        if (Math.random() < 0.5f)
                            newEntity.force = newEntity.force * (1 - coefMutation);
                        else
                            newEntity.force = newEntity.force + (1 - newEntity.force) * coefMutation;
                        if (Math.random() < 0.5f)
                            newEntity.agility = newEntity.agility * (1 - coefMutation);
                        else
                            newEntity.agility = newEntity.agility + (1 - newEntity.agility) * coefMutation;
                        if (Math.random() < 0.5f)
                            newEntity.stamina = newEntity.stamina * (1 - coefMutation);
                        else
                            newEntity.stamina = newEntity.stamina + (1 - newEntity.stamina) * coefMutation;
                        if ((Math.random() < 0.5f)) {
                            if (newEntity.predator == 0f)
                                newEntity.predator = 1f;
                            else
                                newEntity.predator = 0f;
                        }
                    }
                    entities.add(newEntity);
                }
                currentEntity.health -= ((1 / (1 - currentEntity.maxSpeed)) - 1) * coefHealthChange;
                currentEntity.health -= ((1 / (1 - currentEntity.agility)) - 1) * coefHealthChange;
                currentEntity.health -= ((1 / (1 - currentEntity.force)) - 1) * coefHealthChange;
                currentEntity.health -= ((1 / (1 - currentEntity.stamina)) - 1) * coefHealthChange;
                //currentEntity.health -= currentEntity.predator * coefHealthChange;
                if (currentEntity.health <= 0) {
                    entities.remove(currentEntityIndex);
                    currentEntityIndex--;
                }
                sumForce += currentEntity.force;
                sumMaxSpeed += currentEntity.maxSpeed;
                sumStamina += currentEntity.stamina;
                sumAgility += currentEntity.agility;
                sumPredator += currentEntity.predator;
            }
        }
    }
}