import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

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
    public final float coefSpeedDecrease = 1f - (1f - recoverySpeed) / 1024f;
    public final float coefSpeedIncrease = maxSpeed - maxSpeed * (1f - recoverySpeed / 1024f);
    public float maxHealth = 0.5f;
    public float currentHealth = maxHealth;
    public float recoveryHealth = 0.5f;
    public final float coefHealthIncrease = maxHealth - maxHealth * (1f - recoveryHealth / 1024f);
    public float aggressiveness = 0.5f;
    public float immunity = 0.5f;
    public float force = 0.5f;
    public int currentAge;
    public float maxAge = 0.5f;
    public boolean alive = true;
    public boolean estrus = false;
    public int currentEstrusDuration;
    public boolean pregnancy = false;
    public int currentPregnancyDuration;
    public Entity malePartner;

    public Entity (float x, float y) {
        this.x = x;
        this.y = y;
    }
}

public class Main {
    final int W = 800;
    final int H = 800;
    final float maxFullness = 3f;
    final float sightDistance = 128f;
    final float directionChangeRate = 0.001f;
    final float radiusEntity = 8f;
    final float chanceMutation = 1 / 8f;
    final float coefFullnessIncrease = 0.001f;
    final float coefFullnessExcess = 0.00003f / coefFullnessIncrease;
    final float coefFullnessChange = coefFullnessIncrease / 8 * (1 - coefFullnessExcess);
    int logicOnRenderingRate = 32;
    final int estrusDuration = 1024;
    final int pregnancyDuration = 1024;
    final int normalLifeSpan = 65536;
    float sumAggressiveness;
    float sumMaxSpeed;
    float sumForce;
    float sumImmunity;
    float sumMaxAge;
    float sumRecoverySpeed;
    float sumMaxHealth;
    float sumRecoveryHealth;
    ArrayList<Entity> entities = new ArrayList<>();
    int countCycles = 0;
    int countDead = 0;
    float maxDist = sightDistance * sightDistance;
    float maxDistPartner = radiusEntity * radiusEntity;
    int deathFromExhaustion = 0;
    int deathNatural = 0;
    int deathByKilling = 0;
    int deathManually = 0;
    int asexualReproduction = 0;
    int sexualReproduction = 0;
    boolean signatures = false;
    boolean play = true;

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
            JPanel topPanel = new JPanel();
            JButton playPause = new JButton("Play/Pause");
            playPause.addActionListener(e -> playPause());
            JButton slow = new JButton("Slow down (/2)");
            slow.addActionListener(e -> logicOnRenderingRate(0.5f));
            JButton fast = new JButton("Speed up (x2)");
            fast.addActionListener(e -> logicOnRenderingRate(2f));
            JButton addEntity = new JButton("Add entity");
            addEntity.addActionListener(e -> addEntity());
            JButton killHalf = new JButton("Kill half");
            killHalf.addActionListener(e -> killHalf());
            JButton signatures = new JButton("Signatures");
            signatures.addActionListener(e -> signatures());
            topPanel.add(playPause);
            topPanel.add(slow);
            topPanel.add(fast);
            topPanel.add(addEntity);
            topPanel.add(killHalf);
            topPanel.add(signatures);
            Container contentPane = frame.getContentPane();
            contentPane.add("North", topPanel);
            frame.add(new FormPane(), BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private void signatures() {
        signatures = !signatures;
    }

    private void playPause() {
        play = !play;
    }

    public void logicOnRenderingRate(float r) {
        logicOnRenderingRate = (int) (logicOnRenderingRate * r);
        if (logicOnRenderingRate > 4096)
            logicOnRenderingRate = 4096;
        if (logicOnRenderingRate < 1)
            logicOnRenderingRate = 1;
    }

    public void addEntity() {
        entities.add(new Entity((float) (Math.random() * (W)), (float) (Math.random() * (H))));
    }

    public void killHalf() {
        for (Entity a : entities) {
            if (Math.random() < 0.5f) {
                dead(a);
                deathManually++;
            }
        }
    }

    private void dead(Entity currentEntity) {
        currentEntity.aggressiveness = 0;
        currentEntity.immunity = 0;
        currentEntity.force = 0;
        currentEntity.estrus = false;
        currentEntity.pregnancy = false;
        currentEntity.alive = false;
    }

    public class FormPane extends JPanel {
        public FormPane() {
            entities.add(new Entity((float) (Math.random() * (W)), (float) (Math.random() * (H))));
            Timer timer = new Timer(0, e -> repaint());
            timer.start();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(W, H);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int i = 0; i < logicOnRenderingRate; i++)
                if(play)
                    logic();
            draw(g);
        }

        private void draw(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 255));
            g2.fillRect(0, 0, W, H);
            g2.setColor(new Color(0, 0, 0, 255));
            ArrayList<Float> aggressiveness = new ArrayList<>();
            for (Entity a : entities) {
                aggressiveness.add(a.aggressiveness);
            }
            Collections.sort(aggressiveness);
            for (int i = 0; i < aggressiveness.size(); i++) {
                float a = aggressiveness.get(i);
                g2.fillRect(i + 25, 500 - (int) (a * 50), 1, (int) (a * 50));
            }
            ArrayList<Float> immunity = new ArrayList<>();
            for (Entity a : entities) {
                immunity.add(a.immunity);
            }
            Collections.sort(immunity);
            for (int i = 0; i < immunity.size(); i++) {
                float a = immunity.get(i);
                g2.fillRect(i + 25, 600 - (int) (a * 50), 1, (int) (a * 50));
            }
            ArrayList<Float> force = new ArrayList<>();
            for (Entity a : entities) {
                force.add(a.force);
            }
            Collections.sort(force);
            for (int i = 0; i < force.size(); i++) {
                float a = force.get(i);
                g2.fillRect(i + 25, 550 - (int) (a * 50), 1, (int) (a * 50));
            }
            ArrayList<Float> maxSpeed = new ArrayList<>();
            for (Entity a : entities) {
                maxSpeed.add(a.maxSpeed);
            }
            Collections.sort(maxSpeed);
            for (int i = 0; i < maxSpeed.size(); i++) {
                float a = maxSpeed.get(i);
                g2.fillRect(i + 25, 650 - (int) (a * 50), 1, (int) (a * 50));
            }
            for (Entity a : entities) {
                g2.setColor(new Color(Math.round(a.aggressiveness * 255), Math.round(a.immunity * 255), Math.round(a.force * 255), Math.round(a.currentHealth * 255)));
                g2.fillOval((int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)), (int) (radiusEntity * Math.sqrt(a.fullness) * 2), (int) (radiusEntity * Math.sqrt(a.fullness)) * 2);
                g2.setColor(new Color(0, 0, 0, 255));
                if (signatures) {
                    g2.drawString("Ag " + a.aggressiveness, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 50);
                    g2.drawString("Fo " + a.force, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 40);
                    g2.drawString("Im " + a.immunity, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 30);
                    g2.drawString("CH " + a.currentHealth, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 20);
                    g2.drawString("MH " + a.maxHealth, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 10);
                    g2.drawString("RH " + a.recoveryHealth, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)));
                    g2.drawString("CS " + a.currentSpeed, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 10);
                    g2.drawString("MS " + a.maxSpeed, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 20);
                    g2.drawString("RS " + a.recoverySpeed, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 30);
                    g2.drawString("MA " + a.maxAge, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 40);
                }
            }
            g2.setFont(new Font("default", Font.BOLD, 16));
            g2.setColor(new Color(0, 0, 0, 255));
            g2.drawString("Current speed rendering: " + Float.toString(logicOnRenderingRate), 25, 25);
            g2.drawString("Count cycles: " + Float.toString(countCycles), 25, 50);
            g2.drawString("Count entities: " + Float.toString(entities.size()), 25, 75);
            g2.setColor(new Color(255, 0, 0, 255));
            int countAlive = entities.size() - countDead;
            g2.drawString("Average aggressiveness: " + sumAggressiveness / countAlive, 25, 100);
            g2.setColor(new Color(0, 255, 0, 255));
            g2.drawString("Average immunity: " + sumImmunity / countAlive, 25, 125);
            g2.setColor(new Color(0, 0, 255, 255));
            g2.drawString("Average force: " + sumForce / countAlive, 25, 150);
            g2.setColor(new Color(0, 0, 0, 255));
            g2.drawString("Average max speed: " + sumMaxSpeed / countAlive, 25, 175);
            g2.drawString("Average recovery speed: " + sumRecoverySpeed / countAlive, 25, 200);
            g2.drawString("Average max health: " + sumMaxHealth / countAlive, 25, 225);
            g2.drawString("Average recovery health: " + sumRecoveryHealth / countAlive, 25, 250);
            g2.drawString("Average max age: " + sumMaxAge / countAlive, 25, 275);
            g2.drawString("Death from exhaustion: " + deathFromExhaustion, 25, 300);
            g2.drawString("Death by killing: " + deathByKilling, 25, 325);
            g2.drawString("Death natural: " + deathNatural, 25, 350);
            g2.drawString("Death manually: " + deathManually, 25, 375);
            g2.drawString("Asexual reproduction: " + asexualReproduction, 25, 400);
            g2.drawString("Sexual reproduction: " + sexualReproduction, 25, 425);
            g2.setFont(new Font("default", Font.PLAIN, 10));
        }

        private void logic() {
            countCycles++;
            sumAggressiveness = 0;
            sumForce = 0;
            sumMaxSpeed = 0;
            sumRecoverySpeed = 0;
            sumImmunity = 0;
            sumMaxHealth = 0;
            sumRecoveryHealth = 0;
            sumMaxAge = 0;
            countDead = 0;
            for (int currentEntityIndex = 0; currentEntityIndex < entities.size(); currentEntityIndex++) {
                Entity currentEntity = entities.get(currentEntityIndex);
                if (currentEntity.alive) {
                    if (Math.random() < 0.00008 * (1 - currentEntity.immunity) * (currentEntity.currentAge / (normalLifeSpan / (1f - currentEntity.maxAge)))) {
                        dead(currentEntity);
                        deathNatural++;
                        continue;
                    }
                    currentEntity.currentAge++;
                    currentEntity.fullness += coefFullnessIncrease;
                    if (currentEntity.currentHealth < currentEntity.maxHealth)
                        currentEntity.currentHealth += currentEntity.coefHealthIncrease;
                    else if (currentEntity.currentHealth > currentEntity.maxHealth)
                        currentEntity.currentHealth = currentEntity.maxHealth;
                    double targetAngle = Math.atan2(currentEntity.ty, currentEntity.tx);
                    if (currentEntity.useMaxSpeed) {
                        currentEntity.x += (float) Math.cos(targetAngle) * currentEntity.currentSpeed;
                        currentEntity.y += (float) Math.sin(targetAngle) * currentEntity.currentSpeed;
                        if (currentEntity.currentSpeed > currentEntity.maxSpeed / 4f)
                            currentEntity.currentSpeed *= currentEntity.coefSpeedDecrease;
                    } else {
                        currentEntity.x += (float) Math.cos(targetAngle) * currentEntity.currentSpeed / 4f;
                        currentEntity.y += (float) Math.sin(targetAngle) * currentEntity.currentSpeed / 4f;
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
                    float minEnemyDist1 = maxDist;
                    float minEnemyDist2 = maxDist;
                    for (Entity e : entities) {
                        if (currentEntity != e
                                & checkDist(currentEntity, e, sightDistance)
                                & ((alien(currentEntity, e) & currentEntity.aggressiveness < e.aggressiveness)
                                | (insider(currentEntity, e) & currentEntity.aggressiveness < e.aggressiveness & !currentEntity.estrus & !e.estrus))) {
                            float dist = getDist(currentEntity, e);
                            if (dist < minEnemyDist1) {
                                minEnemyDist2 = minEnemyDist1;
                                minEnemyDist1 = dist;
                                closestEnemy2 = closestEnemy1;
                                closestEnemy1 = e;
                            }
                        }
                    }
                    if (closestEnemy1 != null & closestEnemy2 != null)
                        if(alien(closestEnemy1, currentEntity) & !alien(closestEnemy2, currentEntity))
                            closestEnemy2 = null;
                    minEnemyDist2 = (float) Math.sqrt(minEnemyDist2);
                    minEnemyDist1 = (float) Math.sqrt(minEnemyDist1);
                    Entity closestFemalePartner = null;
                    float minFemalePartnerDist = maxDist;
                    if (closestEnemy1 == null
                            & currentEntity.fullness >= 1f
                            & !currentEntity.estrus
                            & !currentEntity.pregnancy
                            & currentEntity.currentHealth == currentEntity.maxHealth)
                        for (Entity e : entities) {
                            if (currentEntity != e
                                    & checkDist(currentEntity, e, sightDistance)
                                    & e.estrus
                                    & insider(currentEntity, e)) {
                                float dist = getDist(currentEntity, e);
                                if (dist < minFemalePartnerDist) {
                                    minFemalePartnerDist = dist;
                                    closestFemalePartner = e;
                                }
                            }
                        }
                    Entity closestFood = null;
                    int indexClosestFood = 0;
                    float minFoodDist = maxDist;
                    if (closestEnemy1 == null
                            & closestFemalePartner == null
                            & !currentEntity.estrus
                            & !currentEntity.pregnancy
                            & currentEntity.currentHealth == currentEntity.maxHealth)
                        for (Entity e : entities) {
                            if (currentEntity != e
                                    & checkDist(currentEntity, e, sightDistance)
                                    & currentEntity.aggressiveness > e.aggressiveness
                                    & alien(currentEntity, e)) {
                                float dist = getDist(currentEntity, e);
                                if (dist < minFoodDist) {
                                    minFoodDist = dist;
                                    closestFood = e;
                                    indexClosestFood = entities.indexOf(e);
                                }
                            }
                        }
                    minFoodDist = (float) Math.sqrt(minFoodDist);
                    Entity closestMalePartner = null;
                    float minMalePartnerDist = maxDistPartner;
                    if (currentEntity.currentEstrusDuration == estrusDuration)
                        for (Entity e : entities) {
                            if (currentEntity != e
                                    & e.alive
                                    & insider(currentEntity, e)
                                    & checkDist(currentEntity, e, radiusEntity)) {
                                float dist = getDist(currentEntity, e);
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
                                if (minFoodDist < radiusEntity) {
                                    while (true) {
                                        closestFood.currentHealth -= currentEntity.force * currentEntity.aggressiveness * currentEntity.force * currentEntity.aggressiveness;
                                        if (closestFood.currentHealth <= 0)
                                            break;
                                        currentEntity.currentHealth -= closestFood.force * closestFood.aggressiveness * closestFood.force * closestFood.aggressiveness;
                                        if (currentEntity.currentHealth <= 0)
                                            break;
                                    }
                                    if (currentEntity.currentHealth > 0) {
                                        currentEntity.fullness += closestFood.fullness * 0.5f;
                                        if (closestFood.alive)
                                            deathByKilling++;
                                        entities.remove(indexClosestFood);
                                        if (indexClosestFood < currentEntityIndex)
                                            currentEntityIndex--;
                                    } else {
                                        closestFood.fullness += currentEntity.fullness * 0.5f;
                                        if (currentEntity.alive)
                                            deathByKilling++;
                                        entities.remove(currentEntityIndex);
                                        continue;
                                    }
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
                    if (currentEntity.ty > sightDistance)
                        currentEntity.ty = H - currentEntity.ty;
                    if (currentEntity.ty < -sightDistance)
                        currentEntity.ty = -H - currentEntity.ty;
                    if (currentEntity.tx > sightDistance)
                        currentEntity.tx = W - currentEntity.tx;
                    if (currentEntity.tx < -sightDistance)
                        currentEntity.tx = -W - currentEntity.tx;
                    if (currentEntity.fullness >= maxFullness & currentEntity.currentHealth == currentEntity.maxHealth & !currentEntity.pregnancy & !currentEntity.estrus)
                        currentEntity.estrus = true;
                    if (currentEntity.estrus) {
                        currentEntity.currentEstrusDuration++;
                    }
                    if (currentEntity.currentEstrusDuration > estrusDuration) {
                        currentEntity.pregnancy = true;
                        currentEntity.estrus = false;
                        currentEntity.currentEstrusDuration = 0;
                        currentEntity.malePartner = closestMalePartner;
                    }
                    if (currentEntity.pregnancy) {
                        currentEntity.currentPregnancyDuration++;
                    }
                    if (currentEntity.currentPregnancyDuration > pregnancyDuration) {
                        reproduction(currentEntity);
                        currentEntity.pregnancy = false;
                        currentEntity.currentPregnancyDuration = 0;
                        currentEntity.malePartner = null;
                    }
                    currentEntity.fullness -= ((1 / (1 - currentEntity.maxSpeed)) - 1) * coefFullnessChange;
                    currentEntity.fullness -= ((1 / (1 - currentEntity.immunity)) - 1) * coefFullnessChange;
                    currentEntity.fullness -= ((1 / (1 - currentEntity.aggressiveness)) - 1) * coefFullnessChange;
                    currentEntity.fullness -= ((1 / (1 - currentEntity.force)) - 1) * coefFullnessChange;
                    currentEntity.fullness -= ((1 / (1 - currentEntity.recoverySpeed)) - 1) * coefFullnessChange;
                    currentEntity.fullness -= ((1 / (1 - currentEntity.maxHealth)) - 1) * coefFullnessChange;
                    currentEntity.fullness -= ((1 / (1 - currentEntity.recoveryHealth)) - 1) * coefFullnessChange;
                    currentEntity.fullness -= ((1 / (1 - currentEntity.maxAge)) - 1) * coefFullnessChange;
                    if (currentEntity.fullness <= 1 / 8f) {
                        dead(currentEntity);
                        deathFromExhaustion++;
                        continue;
                    }
                    sumAggressiveness += currentEntity.aggressiveness;
                    sumForce += currentEntity.force;
                    sumMaxSpeed += currentEntity.maxSpeed;
                    sumRecoverySpeed += currentEntity.recoverySpeed;
                    sumImmunity += currentEntity.immunity;
                    sumMaxHealth += currentEntity.maxHealth;
                    sumRecoveryHealth += currentEntity.recoveryHealth;
                    sumMaxAge += currentEntity.maxAge;
                } else
                    countDead++;
            }
        }

        private void reproduction(Entity currentEntity) {
            currentEntity.fullness = currentEntity.fullness - 2f;
            Entity newEntity = new Entity(currentEntity.x + (float) Math.random() * 10 - 5, currentEntity.y + (float) Math.random() * 10 - 5);
            if (currentEntity.malePartner == null) {
                newEntity.maxSpeed = currentEntity.maxSpeed;
                newEntity.aggressiveness = currentEntity.aggressiveness;
                newEntity.force = currentEntity.force;
                newEntity.immunity = currentEntity.immunity;
                newEntity.recoverySpeed = currentEntity.recoverySpeed;
                newEntity.maxHealth = currentEntity.maxHealth;
                newEntity.recoveryHealth = currentEntity.recoveryHealth;
                newEntity.maxAge = currentEntity.maxAge;
                asexualReproduction++;
            } else {
                newEntity.maxSpeed = 1 - 1 / ((((1 / (1 - currentEntity.maxSpeed)) - 1) + ((1 / (1 - currentEntity.malePartner.maxSpeed)) - 1)) / 2 + 1);
                newEntity.force = 1 - 1 / ((((1 / (1 - currentEntity.force)) - 1) + ((1 / (1 - currentEntity.malePartner.force)) - 1)) / 2 + 1);
                newEntity.aggressiveness = 1 - 1 / ((((1 / (1 - currentEntity.aggressiveness)) - 1) + ((1 / (1 - currentEntity.malePartner.aggressiveness)) - 1)) / 2 + 1);
                newEntity.immunity = 1 - 1 / ((((1 / (1 - currentEntity.immunity)) - 1) + ((1 / (1 - currentEntity.malePartner.immunity)) - 1)) / 2 + 1);
                newEntity.recoverySpeed = 1 - 1 / ((((1 / (1 - currentEntity.recoverySpeed)) - 1) + ((1 / (1 - currentEntity.malePartner.recoverySpeed)) - 1)) / 2 + 1);
                newEntity.maxHealth = 1 - 1 / ((((1 / (1 - currentEntity.maxHealth)) - 1) + ((1 / (1 - currentEntity.malePartner.maxHealth)) - 1)) / 2 + 1);
                newEntity.recoveryHealth = 1 - 1 / ((((1 / (1 - currentEntity.recoveryHealth)) - 1) + ((1 / (1 - currentEntity.malePartner.recoveryHealth)) - 1)) / 2 + 1);
                newEntity.maxAge = 1 - 1 / ((((1 / (1 - currentEntity.maxAge)) - 1) + ((1 / (1 - currentEntity.malePartner.maxAge)) - 1)) / 2 + 1);
                sexualReproduction++;
            }
            if (Math.random() < chanceMutation)
                switch ((int) Math.ceil(Math.random() * 8)) {
                    case 1:
                        if (Math.random() < 0.5f)
                            newEntity.maxSpeed = (float) (newEntity.maxSpeed * (1 - Math.random() * Math.random() * Math.random()));
                        else
                            newEntity.maxSpeed = (float) (newEntity.maxSpeed + (1 - newEntity.maxSpeed) * Math.random() * Math.random() * Math.random());
                        break;
                    case 2:
                        if (Math.random() < 0.5f)
                            newEntity.aggressiveness = (float) (newEntity.aggressiveness * (1 - Math.random() * Math.random() * Math.random()));
                        else
                            newEntity.aggressiveness = (float) (newEntity.aggressiveness + (1 - newEntity.aggressiveness) * Math.random() * Math.random() * Math.random());
                        break;
                    case 3:
                        if (Math.random() < 0.5f)
                            newEntity.immunity = (float) (newEntity.immunity * (1 - Math.random() * Math.random() * Math.random()));
                        else
                            newEntity.immunity = (float) (newEntity.immunity + (1 - newEntity.immunity) * Math.random() * Math.random() * Math.random());
                        break;
                    case 4:
                        if (Math.random() < 0.5f)
                            newEntity.recoverySpeed = (float) (newEntity.recoverySpeed * (1 - Math.random() * Math.random() * Math.random()));
                        else
                            newEntity.recoverySpeed = (float) (newEntity.recoverySpeed + (1 - newEntity.recoverySpeed) * Math.random() * Math.random() * Math.random());
                        break;
                    case 5:
                        if (Math.random() < 0.5f)
                            newEntity.maxHealth = (float) (newEntity.maxHealth * (1 - Math.random() * Math.random() * Math.random()));
                        else
                            newEntity.maxHealth = (float) (newEntity.maxHealth + (1 - newEntity.maxHealth) * Math.random() * Math.random() * Math.random());
                        break;
                    case 6:
                        if (Math.random() < 0.5f)
                            newEntity.recoveryHealth = (float) (newEntity.recoveryHealth * (1 - Math.random() * Math.random() * Math.random()));
                        else
                            newEntity.recoveryHealth = (float) (newEntity.recoveryHealth + (1 - newEntity.recoveryHealth) * Math.random() * Math.random() * Math.random());
                        break;
                    case 7:
                        if (Math.random() < 0.5f)
                            newEntity.maxAge = (float) (newEntity.maxAge * (1 - Math.random() * Math.random() * Math.random()));
                        else
                            newEntity.maxAge = (float) (newEntity.maxAge + (1 - newEntity.maxAge) * Math.random() * Math.random() * Math.random());
                        break;
                    case 8:
                        if (Math.random() < 0.5f)
                            newEntity.force = (float) (newEntity.force * (1 - Math.random() * Math.random() * Math.random()));
                        else
                            newEntity.force = (float) (newEntity.force + (1 - newEntity.force) * Math.random() * Math.random() * Math.random());
                        break;
                }
            newEntity.currentSpeed = newEntity.maxSpeed;
            newEntity.currentHealth = newEntity.maxHealth;
            entities.add(newEntity);
        }

        private boolean twin(Entity currentEntity, Entity e) {
            if (Math.max(((1 / (1 - currentEntity.maxHealth)) - 1), ((1 / (1 - e.maxHealth)) - 1)) / Math.min(((1 / (1 - currentEntity.maxHealth)) - 1), ((1 / (1 - e.maxHealth)) - 1)) > 1.133333f)
                return false;
            if (Math.max(((1 / (1 - currentEntity.recoveryHealth)) - 1), ((1 / (1 - e.recoveryHealth)) - 1)) / Math.min(((1 / (1 - currentEntity.recoveryHealth)) - 1), ((1 / (1 - e.recoveryHealth)) - 1)) > 1.133333f)
                return false;
            if (Math.max(((1 / (1 - currentEntity.aggressiveness)) - 1), ((1 / (1 - e.aggressiveness)) - 1)) / Math.min(((1 / (1 - currentEntity.aggressiveness)) - 1), ((1 / (1 - e.aggressiveness)) - 1)) > 1.133333f)
                return false;
            if (Math.max(((1 / (1 - currentEntity.force)) - 1), ((1 / (1 - e.force)) - 1)) / Math.min(((1 / (1 - currentEntity.force)) - 1), ((1 / (1 - e.force)) - 1)) > 1.133333f)
                return false;
            if (Math.max(((1 / (1 - currentEntity.maxSpeed)) - 1), ((1 / (1 - e.maxSpeed)) - 1)) / Math.min(((1 / (1 - currentEntity.maxSpeed)) - 1), ((1 / (1 - e.maxSpeed)) - 1)) > 1.133333f)
                return false;
            if (Math.max(((1 / (1 - currentEntity.maxAge)) - 1), ((1 / (1 - e.maxAge)) - 1)) / Math.min(((1 / (1 - currentEntity.maxAge)) - 1), ((1 / (1 - e.maxAge)) - 1)) > 1.133333f)
                return false;
            if (Math.max(((1 / (1 - currentEntity.recoverySpeed)) - 1), ((1 / (1 - e.recoverySpeed)) - 1)) / Math.min(((1 / (1 - currentEntity.recoverySpeed)) - 1), ((1 / (1 - e.recoverySpeed)) - 1)) > 1.133333f)
                return false;
            if (Math.max(((1 / (1 - currentEntity.immunity)) - 1), ((1 / (1 - e.immunity)) - 1)) / Math.min(((1 / (1 - currentEntity.immunity)) - 1), ((1 / (1 - e.immunity)) - 1)) > 1.133333f)
                return false;
            return true;
        }

        private boolean alien(Entity currentEntity, Entity e) {
            if (Math.max(((1 / (1 - currentEntity.maxHealth)) - 1), ((1 / (1 - e.maxHealth)) - 1)) / Math.min(((1 / (1 - currentEntity.maxHealth)) - 1), ((1 / (1 - e.maxHealth)) - 1)) > 3f)
                return true;
            if (Math.max(((1 / (1 - currentEntity.recoveryHealth)) - 1), ((1 / (1 - e.recoveryHealth)) - 1)) / Math.min(((1 / (1 - currentEntity.recoveryHealth)) - 1), ((1 / (1 - e.recoveryHealth)) - 1)) > 3f)
                return true;
            if (Math.max(((1 / (1 - currentEntity.aggressiveness)) - 1), ((1 / (1 - e.aggressiveness)) - 1)) / Math.min(((1 / (1 - currentEntity.aggressiveness)) - 1), ((1 / (1 - e.aggressiveness)) - 1)) > 3f)
                return true;
            if (Math.max(((1 / (1 - currentEntity.force)) - 1), ((1 / (1 - e.force)) - 1)) / Math.min(((1 / (1 - currentEntity.force)) - 1), ((1 / (1 - e.force)) - 1)) > 3f)
                return true;
            if (Math.max(((1 / (1 - currentEntity.maxSpeed)) - 1), ((1 / (1 - e.maxSpeed)) - 1)) / Math.min(((1 / (1 - currentEntity.maxSpeed)) - 1), ((1 / (1 - e.maxSpeed)) - 1)) > 3f)
                return true;
            if (Math.max(((1 / (1 - currentEntity.maxAge)) - 1), ((1 / (1 - e.maxAge)) - 1)) / Math.min(((1 / (1 - currentEntity.maxAge)) - 1), ((1 / (1 - e.maxAge)) - 1)) > 3f)
                return true;
            if (Math.max(((1 / (1 - currentEntity.recoverySpeed)) - 1), ((1 / (1 - e.recoverySpeed)) - 1)) / Math.min(((1 / (1 - currentEntity.recoverySpeed)) - 1), ((1 / (1 - e.recoverySpeed)) - 1)) > 3f)
                return true;
            if (Math.max(((1 / (1 - currentEntity.immunity)) - 1), ((1 / (1 - e.immunity)) - 1)) / Math.min(((1 / (1 - currentEntity.immunity)) - 1), ((1 / (1 - e.immunity)) - 1)) > 3f)
                return true;
            return false;
        }

        private boolean insider(Entity currentEntity, Entity e) {
            return !twin(currentEntity, e) & !alien(currentEntity, e);
        }

        private float getDist(Entity currentEntity, Entity e) {
            float dist1 = (currentEntity.x - e.x) * (currentEntity.x - e.x) + (currentEntity.y - e.y) * (currentEntity.y - e.y);
            float dist2 = (W - Math.abs(currentEntity.x - e.x)) * (W - Math.abs(currentEntity.x - e.x)) + (currentEntity.y - e.y) * (currentEntity.y - e.y);
            float dist3 = (currentEntity.x - e.x) * (currentEntity.x - e.x) + (H - Math.abs(currentEntity.y - e.y)) * (H - Math.abs(currentEntity.y - e.y));
            float dist4 = (W - Math.abs(currentEntity.x - e.x)) * (W - Math.abs(currentEntity.x - e.x)) + (H - Math.abs(currentEntity.y - e.y)) * (H - Math.abs(currentEntity.y - e.y));
            return Math.min(Math.min(dist1, dist2), Math.min(dist3, dist4));
        }

        private boolean checkDist(Entity currentEntity, Entity e, float sightDistance) {
            return (Math.abs(currentEntity.x - e.x) < sightDistance) | (W - Math.abs(currentEntity.x - e.x) < sightDistance)
                    & (Math.abs(currentEntity.y - e.y) < sightDistance) | (H - Math.abs(currentEntity.y - e.y) < sightDistance);
        }
    }
}