import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class Entity  {
    public float x;
    public float y;
    public float tx = 0;
    public float ty = 0;
    public float fullness = 1f;
    public float maxSpeed = 0.5f;
    public float currentSpeed = maxSpeed;
    public float recoverySpeed = 0.5f;
    public boolean useMaxSpeed = false;
    public final float coefSpeedDecrease = 1f - (1f - recoverySpeed) / 16f;
    public final float coefSpeedIncrease = maxSpeed - maxSpeed * (1f - recoverySpeed / 16f);
    public float maxHealth = 0.5f;
    public float currentHealth = maxHealth;
    public float recoveryHealth = 0.5f;
    public final float coefHealthIncrease = maxHealth - maxHealth * (1f - recoveryHealth / 16f);
    public float force = 0.5f;
    public float toxicity = 0.5f;
    public float intelligence = 0.5f;
    public int currentAge;
    public float maxAge = 0.5f;
    public boolean alive = true;
    public boolean estrus = false;
    public int currentEstrusDuration;


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
    float sumToxicity;
    float sumMaxAge;
    float sumRecoverySpeed;
    float sumIntelligence;
    float sumMaxHealth;
    float sumRecoveryHealth;
    final float maxFullness = 3f;
    final float sightDistance = 100f;
    final float directionChangeRate = 0.01f;
    final float radiusEntity = 10f;
    final float chanceMutation = 0.1f;
    final float coefFullnessChange = 0.00003f;
    final float coefFullnessIncrease = 0.001f;
    final float coefMutation = 0.2f;
    ArrayList<Entity> entities = new ArrayList<>();
    int countCycles = 0;
    int countDead = 0;
    public int refreshRate = 8;
    public float minExcess = 1f + 1 / 8f;
    public int EstrusDuration = 256;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {
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
            Timer timer = new Timer(refreshRate, e -> repaint());
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
            int countAlive = entities.size() - countDead;
            g2.drawString("Average force: " + sumForce / countAlive, 50, 150);
            g2.setColor(new Color(0, 255, 0, 255));
            g2.drawString("Average toxicity: " + sumToxicity / countAlive, 50, 200);
            g2.setColor(new Color(0, 0, 255, 255));
            g2.drawString("Average intelligence: " + sumIntelligence / countAlive, 50, 250);
            g2.setColor(new Color(0, 0, 0, 255));
            g2.drawString("Average max speed: " + sumMaxSpeed / countAlive, 50, 300);
            g2.drawString("Average recovery speed: " + sumRecoverySpeed / countAlive, 50, 350);
            g2.drawString("Average max health: " + sumMaxHealth / countAlive, 50, 400);
            g2.drawString("Average recovery health: " + sumRecoveryHealth / countAlive, 50, 450);
            g2.drawString("Average max age: " + sumMaxAge / countAlive, 50, 500);
            for (Entity a : entities) {
                g2.setColor(new Color(Math.round(a.force * 255), Math.round(a.toxicity * 255), Math.round(a.intelligence * 255), Math.round(a.currentHealth * 255)));
                g2.fillOval((int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)), (int) (radiusEntity * Math.sqrt(a.fullness) * 2), (int) (radiusEntity * Math.sqrt(a.fullness)) * 2);
                g2.setColor(new Color(0, 0, 0, 255));
                g2.drawString("Fo " + a.force, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 50);
                g2.drawString("In " + a.intelligence, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 40);
                g2.drawString("To " + a.toxicity, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 30);
                g2.drawString("CH " + a.currentHealth, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 20);
                g2.drawString("MH " + a.maxHealth, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 10);
                g2.drawString("RH " + a.recoveryHealth, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)));
                g2.drawString("CS " + a.currentSpeed, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 10);
                g2.drawString("MS " + a.maxSpeed, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 20);
                g2.drawString("RS " + a.recoverySpeed, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 30);
                g2.drawString("MA " + a.maxAge, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 40);
                g2.drawString("ED " + a.currentEstrusDuration, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 50);
                g2.drawString("LL " + (100f - Math.round(a.currentAge / (2500 / (1f - a.maxAge)) * 100f)), (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 60);
            }
        }

        private void logic() {
            countCycles++;
            sumForce = 0;
            sumMaxSpeed = 0;
            sumRecoverySpeed = 0;
            sumToxicity = 0;
            sumMaxHealth = 0;
            sumRecoveryHealth = 0;
            sumIntelligence = 0;
            sumMaxAge = 0;
            countDead = 0;
            for (int currentEntityIndex = 0; currentEntityIndex < entities.size(); currentEntityIndex++) {
                Entity currentEntity = entities.get(currentEntityIndex);
                if (currentEntity.alive) {
                    currentEntity.currentAge++;
                    currentEntity.fullness += coefFullnessIncrease;
                    if (currentEntity.currentHealth < currentEntity.maxHealth)
                        currentEntity.currentHealth += currentEntity.coefHealthIncrease;
                    else if (currentEntity.currentHealth > currentEntity.maxHealth)
                        currentEntity.currentHealth = currentEntity.maxHealth;
                    double targetAngle = Math.atan2(currentEntity.ty, currentEntity.tx);
                    if (currentEntity.useMaxSpeed) {
                        currentEntity.x += (float) Math.cos(targetAngle) * currentEntity.currentSpeed * refreshRate;
                        currentEntity.y += (float) Math.sin(targetAngle) * currentEntity.currentSpeed * refreshRate;
                        if (currentEntity.currentSpeed > currentEntity.maxSpeed / 4f)
                            currentEntity.currentSpeed *= currentEntity.coefSpeedDecrease;
                    } else {
                        currentEntity.x += (float) Math.cos(targetAngle) * currentEntity.maxSpeed / 4f * refreshRate;
                        currentEntity.y += (float) Math.sin(targetAngle) * currentEntity.maxSpeed / 4f * refreshRate;
                        currentEntity.currentSpeed += currentEntity.coefSpeedIncrease;
                        if (currentEntity.currentSpeed > currentEntity.maxSpeed)
                            currentEntity.currentSpeed = currentEntity.maxSpeed;
                    }
                    if (currentEntity.x < 0) currentEntity.x = W;
                    else if (currentEntity.x > W) currentEntity.x = 0;
                    if (currentEntity.y < 0) currentEntity.y = H;
                    else if (currentEntity.y > H) currentEntity.y = 0;
                    Entity closestEnemy1 = null;
                    Entity closestEnemy2 = null;
                    float minEnemyDist1 = (W * W) + (H * H);
                    float minEnemyDist2 = (W * W) + (H * H);
                    for (Entity e : entities) {
                        if (currentEntity == e)
                            continue;
                        if (currentEntity.intelligence >= e.intelligence) {
                            if (currentEntity.force * minExcess >= e.force * (e.intelligence / currentEntity.intelligence))
                                continue;
                        } else if (currentEntity.force * minExcess >= e.force * (currentEntity.intelligence / e.intelligence))
                            continue;
                        if (currentEntity.force >= e.force - Math.abs(currentEntity.intelligence - e.intelligence))
                            continue;
                        float dist = getDist(currentEntity, e);
                        if (dist < sightDistance * sightDistance) {
                            if (dist < minEnemyDist1) {
                                minEnemyDist2 = minEnemyDist1;
                                minEnemyDist1 = dist;
                                closestEnemy2 = closestEnemy1;
                                closestEnemy1 = e;
                            }
                        }
                    }
                    minEnemyDist2 = (float) Math.sqrt(minEnemyDist2);
                    minEnemyDist1 = (float) Math.sqrt(minEnemyDist1);
                    Entity closestFemalePartner = null;
                    float minFemalePartnerDist = (W * W) + (H * H);
                    if (closestEnemy1 == null
                            & currentEntity.fullness >= 1f
                            & !currentEntity.estrus
                            & currentEntity.currentHealth == currentEntity.maxHealth)
                        for (Entity e : entities) {
                            if (currentEntity == e)
                                continue;
                            if (currentEntity.intelligence >= e.intelligence) {
                                if (currentEntity.force >= e.force * minExcess)
                                    continue;
                            } else if (currentEntity.force >= e.force * (e.intelligence / currentEntity.intelligence) * minExcess)
                                continue;
                            if (!e.estrus)
                                continue;
                            if (twin(currentEntity, e)) continue;
                            float dist = getDist(currentEntity, e);
                            if (dist < sightDistance * sightDistance) {
                                if (dist < minFemalePartnerDist) {
                                    minFemalePartnerDist = dist;
                                    closestFemalePartner = e;
                                }
                            }
                        }
                    Entity closestFood = null;
                    int indexClosestFood = 0;
                    float minFoodDist = (W * W) + (H * H);
                    if (closestEnemy1 == null
                            & closestFemalePartner == null
                            & currentEntity.currentHealth == currentEntity.maxHealth)
                        for (Entity e : entities) {
                            if (currentEntity == e)
                                continue;
                            if (currentEntity.intelligence >= e.intelligence) {
                                if (currentEntity.force <= e.force * minExcess)
                                    continue;
                            } else if (currentEntity.force <= e.force * (e.intelligence / currentEntity.intelligence) * minExcess)
                                continue;
                            float dist = getDist(currentEntity, e);
                            if (dist < sightDistance * sightDistance) {
                                if (dist < minFoodDist) {
                                    minFoodDist = dist;
                                    closestFood = e;
                                    indexClosestFood = entities.indexOf(e);
                                }
                            }
                        }
                    Entity closestMalePartner = null;
                    float minMalePartnerDist = (W * W) + (H * H);
                    if (closestEnemy1 == null
                            & currentEntity.currentEstrusDuration == EstrusDuration)
                        for (Entity e : entities) {
                            if (currentEntity == e
                                    | !e.alive
                                    | e == closestFood
                                    | twin(currentEntity, e))
                                continue;
                            float dist = getDist(currentEntity, e);
                            if (dist < radiusEntity * radiusEntity) {
                                if (dist < minMalePartnerDist) {
                                    minMalePartnerDist = dist;
                                    closestMalePartner = e;
                                }
                            }
                        }
                    currentEntity.useMaxSpeed = true;
                    if (closestEnemy1 != null) {
                        if (Math.abs(closestEnemy1.x - currentEntity.x) > sightDistance)
                            currentEntity.tx = closestEnemy1.x - currentEntity.x;
                        else
                            currentEntity.tx = -closestEnemy1.x + currentEntity.x;
                        if (Math.abs(closestEnemy1.y - currentEntity.y) > sightDistance)
                            currentEntity.ty = closestEnemy1.y - currentEntity.y;
                        else
                            currentEntity.ty = -closestEnemy1.y + currentEntity.y;
                        if (closestEnemy2 != null & minEnemyDist1 < sightDistance / 2 & minEnemyDist2 < sightDistance / 2) {
                            if (Math.abs(closestEnemy2.x - currentEntity.x) > sightDistance)
                                currentEntity.tx += closestEnemy2.x - currentEntity.x;
                            else
                                currentEntity.tx += -closestEnemy2.x + currentEntity.x;
                            if (Math.abs(closestEnemy2.y - currentEntity.y) > sightDistance)
                                currentEntity.ty += closestEnemy2.y - currentEntity.y;
                            else
                                currentEntity.ty += -closestEnemy2.y + currentEntity.y;
                        }
                    } else {
                        if (closestFemalePartner != null) {
                            if (Math.abs(closestFemalePartner.x - currentEntity.x) > sightDistance)
                                currentEntity.tx = -closestFemalePartner.x + currentEntity.x;
                            else
                                currentEntity.tx = closestFemalePartner.x - currentEntity.x;
                            if (Math.abs(closestFemalePartner.y - currentEntity.y) > sightDistance)
                                currentEntity.ty = -closestFemalePartner.y + currentEntity.y;
                            else
                                currentEntity.ty = closestFemalePartner.y - currentEntity.y;
                        } else {
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
                                    currentEntity.currentHealth *= 1 - (closestFood.force / currentEntity.force) * (closestFood.force / currentEntity.force);
                                    currentEntity.fullness += closestFood.fullness * (0.75f - closestFood.toxicity);
                                    entities.remove(indexClosestFood);
                                    if (indexClosestFood < currentEntityIndex)
                                        currentEntityIndex--;
                                }
                            } else {
                                currentEntity.useMaxSpeed = false;
                                if (Math.random() < directionChangeRate) {
                                    double randomAngle = Math.random() * Math.PI * 2;
                                    currentEntity.tx = (float) Math.cos(randomAngle);
                                    currentEntity.ty = (float) Math.sin(randomAngle);
                                }
                            }
                        }
                    }
                    if (currentEntity.fullness >= maxFullness & currentEntity.currentHealth == currentEntity.maxHealth)
                        currentEntity.estrus = true;
                    if (currentEntity.estrus) {
                        currentEntity.currentEstrusDuration++;
                        if (currentEntity.currentEstrusDuration > EstrusDuration) {
                            currentEntity.fullness = currentEntity.fullness - 2f;
                            Entity newEntity = new Entity(currentEntity.x + (float) Math.random() * 10 - 5, currentEntity.y + (float) Math.random() * 10 - 5);
                            if (closestMalePartner == null) {
                                newEntity.maxSpeed = currentEntity.maxSpeed;
                                newEntity.force = currentEntity.force;
                                newEntity.toxicity = currentEntity.toxicity;
                                newEntity.recoverySpeed = currentEntity.recoverySpeed;
                                newEntity.maxHealth = currentEntity.maxHealth;
                                newEntity.recoveryHealth = currentEntity.recoveryHealth;
                                newEntity.intelligence = currentEntity.intelligence;
                                newEntity.maxAge = currentEntity.maxAge;
                            } else {
                                newEntity.maxSpeed = 1 - 1 / ((((1 / (1 - currentEntity.maxSpeed)) - 1) + ((1 / (1 - closestMalePartner.maxSpeed)) - 1)) / 2 + 1);
                                newEntity.force = 1 - 1 / ((((1 / (1 - currentEntity.force)) - 1) + ((1 / (1 - closestMalePartner.force)) - 1)) / 2 + 1);
                                newEntity.toxicity = 1 - 1 / ((((1 / (1 - currentEntity.toxicity)) - 1) + ((1 / (1 - closestMalePartner.toxicity)) - 1)) / 2 + 1);
                                newEntity.recoverySpeed = 1 - 1 / ((((1 / (1 - currentEntity.recoverySpeed)) - 1) + ((1 / (1 - closestMalePartner.recoverySpeed)) - 1)) / 2 + 1);
                                newEntity.maxHealth = 1 - 1 / ((((1 / (1 - currentEntity.maxHealth)) - 1) + ((1 / (1 - closestMalePartner.maxHealth)) - 1)) / 2 + 1);
                                newEntity.recoveryHealth = 1 - 1 / ((((1 / (1 - currentEntity.recoveryHealth)) - 1) + ((1 / (1 - closestMalePartner.recoveryHealth)) - 1)) / 2 + 1);
                                newEntity.intelligence = 1 - 1 / ((((1 / (1 - currentEntity.intelligence)) - 1) + ((1 / (1 - closestMalePartner.intelligence)) - 1)) / 2 + 1);
                                newEntity.maxAge = 1 - 1 / ((((1 / (1 - currentEntity.maxAge)) - 1) + ((1 / (1 - closestMalePartner.maxAge)) - 1)) / 2 + 1);
                            }
                            if (Math.random() < chanceMutation) {
                                if (Math.random() < 0.5f)
                                    newEntity.maxSpeed = (float) (newEntity.maxSpeed * (1 - Math.random() * coefMutation));
                                else
                                    newEntity.maxSpeed = (float) (newEntity.maxSpeed + (1 - newEntity.maxSpeed) * Math.random() * coefMutation);
                                if (Math.random() < 0.5f)
                                    newEntity.force = (float) (newEntity.force * (1 - Math.random() * coefMutation));
                                else
                                    newEntity.force = (float) (newEntity.force + (1 - newEntity.force) * Math.random() * coefMutation);
                                if (Math.random() < 0.5f)
                                    newEntity.toxicity = (float) (newEntity.toxicity * (1 - Math.random() * coefMutation));
                                else
                                    newEntity.toxicity = (float) (newEntity.toxicity + (1 - newEntity.toxicity) * Math.random() * coefMutation);
                                if (Math.random() < 0.5f)
                                    newEntity.recoverySpeed = (float) (newEntity.recoverySpeed * (1 - Math.random() * coefMutation));
                                else
                                    newEntity.recoverySpeed = (float) (newEntity.recoverySpeed + (1 - newEntity.recoverySpeed) * Math.random() * coefMutation);
                                if (Math.random() < 0.5f)
                                    newEntity.maxHealth = (float) (newEntity.maxHealth * (1 - Math.random() * coefMutation));
                                else
                                    newEntity.maxHealth = (float) (newEntity.maxHealth + (1 - newEntity.maxHealth) * Math.random() * coefMutation);
                                if (Math.random() < 0.5f)
                                    newEntity.recoveryHealth = (float) (newEntity.recoveryHealth * (1 - Math.random() * coefMutation));
                                else
                                    newEntity.recoveryHealth = (float) (newEntity.recoveryHealth + (1 - newEntity.recoveryHealth) * Math.random() * coefMutation);
                                if (Math.random() < 0.5f)
                                    newEntity.maxAge = (float) (newEntity.maxAge * (1 - Math.random() * coefMutation));
                                else
                                    newEntity.maxAge = (float) (newEntity.maxAge + (1 - newEntity.maxAge) * Math.random() * coefMutation);
                                if (Math.random() < 0.5f)
                                    newEntity.intelligence = (float) (newEntity.intelligence * (1 - Math.random() * coefMutation));
                                else
                                    newEntity.intelligence = (float) (newEntity.intelligence + (1 - newEntity.intelligence) * Math.random() * coefMutation);
                            }
                            newEntity.maxSpeed = Math.round(newEntity.maxSpeed * 1000f) / 1000f;
                            newEntity.force = Math.round(newEntity.force * 1000f) / 1000f;
                            newEntity.toxicity = Math.round(newEntity.toxicity * 1000f) / 1000f;
                            newEntity.recoverySpeed = Math.round(newEntity.recoverySpeed * 1000f) / 1000f;
                            newEntity.maxHealth = Math.round(newEntity.maxHealth * 1000f) / 1000f;
                            newEntity.recoveryHealth = Math.round(newEntity.recoveryHealth * 1000f) / 1000f;
                            newEntity.maxAge = Math.round(newEntity.maxAge * 1000f) / 1000f;
                            newEntity.intelligence = Math.round(newEntity.intelligence * 1000f) / 1000f;
                            entities.add(newEntity);
                            currentEntity.estrus = false;
                            currentEntity.currentEstrusDuration = 0;
                        }
                    }
                    currentEntity.fullness -= ((1 / (1 - currentEntity.maxSpeed)) - 1) * coefFullnessChange;
                    currentEntity.fullness -= ((1 / (1 - currentEntity.toxicity)) - 1) * coefFullnessChange;
                    currentEntity.fullness -= ((1 / (1 - currentEntity.force)) - 1) * coefFullnessChange;
                    currentEntity.fullness -= ((1 / (1 - currentEntity.recoverySpeed)) - 1) * coefFullnessChange;
                    currentEntity.fullness -= ((1 / (1 - currentEntity.maxHealth)) - 1) * coefFullnessChange;
                    currentEntity.fullness -= ((1 / (1 - currentEntity.recoveryHealth)) - 1) * coefFullnessChange;
                    currentEntity.fullness -= ((1 / (1 - currentEntity.intelligence)) - 1) * coefFullnessChange;
                    currentEntity.fullness -= ((1 / (1 - currentEntity.maxAge)) - 1) * coefFullnessChange;
                    if (currentEntity.fullness <= 1 / 8f) {
                        dead(currentEntity);
                    }
                    if (currentEntity.currentAge >= 2500 / (1f - currentEntity.maxAge)) {
                        dead(currentEntity);
                    }
                    currentEntity.currentSpeed = Math.round(currentEntity.currentSpeed * 1000f) / 1000f;
                    currentEntity.currentHealth = Math.round(currentEntity.currentHealth * 1000f) / 1000f;
                    sumForce += currentEntity.force;
                    sumMaxSpeed += currentEntity.maxSpeed;
                    sumRecoverySpeed += currentEntity.recoverySpeed;
                    sumToxicity += currentEntity.toxicity;
                    sumMaxHealth += currentEntity.maxHealth;
                    sumRecoveryHealth += currentEntity.recoveryHealth;
                    sumIntelligence += currentEntity.intelligence;
                    sumMaxAge += currentEntity.maxAge;
                } else
                    countDead++;
            }
        }

        private boolean twin(Entity currentEntity, Entity e) {
            return currentEntity.maxHealth == e.maxHealth
                    & currentEntity.recoveryHealth == e.recoveryHealth
                    & currentEntity.force == e.force
                    & currentEntity.intelligence == e.intelligence
                    & currentEntity.maxSpeed == e.maxSpeed
                    & currentEntity.maxAge == e.maxAge
                    & currentEntity.recoverySpeed == e.recoverySpeed
                    & currentEntity.toxicity == e.toxicity;
        }

        private void dead(Entity currentEntity) {
            currentEntity.force = 0;
            currentEntity.maxSpeed = 0;
            currentEntity.intelligence = 0;
            currentEntity.estrus = false;
            currentEntity.alive = false;
        }

        private float getDist(Entity currentEntity, Entity e) {
            float dist1 = (currentEntity.x - e.x) * (currentEntity.x - e.x) + (currentEntity.y - e.y) * (currentEntity.y - e.y);
            float dist2 = (W - Math.abs(currentEntity.x - e.x)) * (W - Math.abs(currentEntity.x - e.x)) + (currentEntity.y - e.y) * (currentEntity.y - e.y);
            float dist3 = (currentEntity.x - e.x) * (currentEntity.x - e.x) + (H - Math.abs(currentEntity.y - e.y)) * (H - Math.abs(currentEntity.y - e.y));
            float dist4 = (W - Math.abs(currentEntity.x - e.x)) * (W - Math.abs(currentEntity.x - e.x)) + (H - Math.abs(currentEntity.y - e.y)) * (H - Math.abs(currentEntity.y - e.y));
            return Math.min(Math.min(dist1, dist2), Math.min(dist3, dist4));
        }
    }
}