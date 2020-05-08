import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

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
    public Status tmpStatus;
    enum Status {
        alien,
        insider,
        twin
    }
    public boolean tmpCheckDist;
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
    final int countParam = 8;
    final float initialCosts = 0.15470053f;
    final float coefFullnessIncrease = 0.001f;
    final float coefFullnessExcess = 0.00005f / coefFullnessIncrease;
    final float coefFullnessChange = coefFullnessIncrease / countParam / initialCosts * (1 - coefFullnessExcess);
    final int estrusDuration = 2048;
    final int pregnancyDuration = 2048;
    final int maxLifeSpan = 262144;
    final float coefNaturalDead = 0.0001f;
    final float minFullness = 1 / 4f;
    final float capacityFactor = 50f;
    final float maxDist = sightDistance * sightDistance;
    final float maxDistPartner = radiusEntity * radiusEntity;
    ArrayList<Entity> entities = new ArrayList<>();
    int logicOnRenderingRate = 32;
    int countCycles = 0;
    int countDead = 0;
    int deathFromExhaustion = 0;
    int deathNatural = 0;
    int deathByKillingAttacking = 0;
    int deathByKillingDefender = 0;
    int deathManually = 0;
    int asexualReproduction = 0;
    int sexualReproduction = 0;
    boolean signatures = false;
    boolean play = true;
    boolean statistics = false;

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
            JButton statistics = new JButton("Statistics");
            statistics.addActionListener(e -> statistics());
            JButton signatures = new JButton("Signatures");
            signatures.addActionListener(e -> signatures());
            JButton addEntity = new JButton("Add entity");
            addEntity.addActionListener(e -> addEntity());
            JButton killHalf = new JButton("Kill half");
            killHalf.addActionListener(e -> killHalf());
            JButton reset = new JButton("Reset");
            reset.addActionListener(e -> reset());
            topPanel.add(playPause);
            topPanel.add(slow);
            topPanel.add(fast);
            topPanel.add(statistics);
            topPanel.add(signatures);
            topPanel.add(addEntity);
            topPanel.add(killHalf);
            topPanel.add(reset);
            Container contentPane = frame.getContentPane();
            contentPane.add("North", topPanel);
            frame.add(new FormPane(), BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private void statistics() {
        statistics = !statistics;
    }

    private void signatures() {
        signatures = !signatures;
    }

    private void reset() {
        entities.clear();
        logicOnRenderingRate = 32;
        countCycles = 0;
        countDead = 0;
        deathFromExhaustion = 0;
        deathNatural = 0;
        deathByKillingAttacking = 0;
        deathByKillingDefender = 0;
        deathManually = 0;
        asexualReproduction = 0;
        sexualReproduction = 0;
        signatures = false;
        play = true;
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
                if (play)
                    logic();
            draw(g);
        }

        private void draw(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 255));
            g2.fillRect(0, 0, W, H);
            g2.setColor(new Color(0, 0, 0, 255));
            if (statistics) {
                for (int i = 0; i < entities.size(); i++) {
                    float a = entities.get(i).aggressiveness;
                    g2.fillRect(i * 3 + 200, 100 - (int) (a * 50), 1, (int) (a * 50));
                }
                for (int i = 0; i < entities.size(); i++) {
                    float a = entities.get(i).immunity;
                    g2.fillRect(i * 3 + 200, 150 - (int) (a * 50), 1, (int) (a * 50));
                }
                for (int i = 0; i < entities.size(); i++) {
                    float a = entities.get(i).force;
                    g2.fillRect(i * 3 + 200, 200 - (int) (a * 50), 1, (int) (a * 50));
                }
                for (int i = 0; i < entities.size(); i++) {
                    float a = entities.get(i).maxSpeed;
                    g2.fillRect(i * 3 + 200, 250 - (int) (a * 50), 1, (int) (a * 50));
                }
                for (int i = 0; i < entities.size(); i++) {
                    float a = entities.get(i).recoverySpeed;
                    g2.fillRect(i * 3 + 200, 300 - (int) (a * 50), 1, (int) (a * 50));
                }
                for (int i = 0; i < entities.size(); i++) {
                    float a = entities.get(i).maxHealth;
                    g2.fillRect(i * 3 + 200, 350 - (int) (a * 50), 1, (int) (a * 50));
                }
                for (int i = 0; i < entities.size(); i++) {
                    float a = entities.get(i).recoveryHealth;
                    g2.fillRect(i * 3 + 200, 400 - (int) (a * 50), 1, (int) (a * 50));
                }
                for (int i = 0; i < entities.size(); i++) {
                    float a = entities.get(i).maxAge;
                    g2.fillRect(i * 3 + 200, 450 - (int) (a * 50), 1, (int) (a * 50));
                }
            }
            for (Entity a : entities) {
                g2.setColor(new Color(Math.round(a.aggressiveness * 255), Math.round(a.immunity * 255), Math.round(a.force * 255), Math.round(a.currentHealth * 255)));
                g2.fillOval((int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)), (int) (radiusEntity * Math.sqrt(a.fullness) * 2), (int) (radiusEntity * Math.sqrt(a.fullness)) * 2);
                g2.setColor(new Color(0, 0, 0, 255));
                if (signatures) {
                    g2.drawString("Ag " + Math.round(a.aggressiveness * 100) / 100f, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 50);
                    g2.drawString("Fo " + Math.round(a.force * 100) / 100f, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 40);
                    g2.drawString("Im " + Math.round(a.immunity * 100) / 100f, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 30);
                    g2.drawString("CH " + Math.round(a.currentHealth * 100) / 100f, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 20);
                    g2.drawString("MH " + Math.round(a.maxHealth * 100) / 100f, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) - 10);
                    g2.drawString("RH " + Math.round(a.recoveryHealth * 100) / 100f, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)));
                    g2.drawString("CS " + Math.round(a.currentSpeed * 100) / 100f, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 10);
                    g2.drawString("MS " + Math.round(a.maxSpeed * 100) / 100f, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 20);
                    g2.drawString("RS " + Math.round(a.recoverySpeed * 100) / 100f, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 30);
                    g2.drawString("MA " + Math.round(a.maxAge * 100) / 100f, (int) (a.x - radiusEntity * Math.sqrt(a.fullness)), (int) (a.y - radiusEntity * Math.sqrt(a.fullness)) + 40);
                }
            }

            g2.setFont(new Font("default", Font.BOLD, 16));
            g2.setColor(new Color(0, 0, 0, 255));
            g2.drawString("Count cycles: " + countCycles, 25, 25);
            g2.drawString("Count entities: " + entities.size(), 275, 25);
            g2.drawString("Current speed rendering: " + logicOnRenderingRate, 525, 25);
            if (statistics) {
                g2.setColor(new Color(255, 0, 0, 255));
                int countAlive = entities.size() - countDead;
                g2.drawString("Aggressiveness: ", 25, 100);
                g2.setColor(new Color(0, 255, 0, 255));
                g2.drawString("Immunity: ", 25, 150);
                g2.setColor(new Color(0, 0, 255, 255));
                g2.drawString("Force: ", 25, 200);
                g2.setColor(new Color(0, 0, 0, 255));
                g2.drawString("Max speed: ", 25, 250);
                g2.drawString("Recovery speed: ", 25, 300);
                g2.drawString("Max health: ", 25, 350);
                g2.drawString("Recovery health: ", 25, 400);
                g2.drawString("Max age: ", 25, 450);
                g2.drawString("Death from exhaustion: " + deathFromExhaustion, 25, 525);
                g2.drawString("Death by killing attacking: " + deathByKillingAttacking, 25, 550);
                g2.drawString("Death by killing defenfer: " + deathByKillingDefender, 25, 575);
                g2.drawString("Death natural: " + deathNatural, 25, 600);
                g2.drawString("Death manually: " + deathManually, 25, 625);
                g2.drawString("Asexual reproduction: " + asexualReproduction, 25, 650);
                g2.drawString("Sexual reproduction: " + sexualReproduction, 25, 675);
                g2.setFont(new Font("default", Font.PLAIN, 10));
            }
        }

        private void logic() {
            countCycles++;
            countDead = 0;
            for (int currentEntityIndex = 0; currentEntityIndex < entities.size(); currentEntityIndex++) {
                Entity currentEntity = entities.get(currentEntityIndex);
                if (currentEntity.alive) {
                    for (Entity e : entities) {
                        if (twin(currentEntity, e))
                            e.tmpStatus = Entity.Status.twin;
                        else if (alien(currentEntity, e))
                            e.tmpStatus = Entity.Status.alien;
                        else
                            e.tmpStatus = Entity.Status.insider;
                        e.tmpCheckDist = checkDist(currentEntity, e, sightDistance);
                    }
                    if (Math.random() < coefNaturalDead * (1 - currentEntity.immunity) * (currentEntity.currentAge / (maxLifeSpan * currentEntity.maxAge))) {
                        dead(currentEntity);
                        deathNatural++;
                        continue;
                    }
                    currentEntity.currentAge++;
                    if ((entities.size() - countDead) <= capacityFactor)
                        currentEntity.fullness += coefFullnessIncrease;
                    else
                        currentEntity.fullness += coefFullnessIncrease * capacityFactor / (entities.size() - countDead);
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
                                & e.tmpCheckDist
                                & ((e.tmpStatus == Entity.Status.alien & currentEntity.aggressiveness < e.aggressiveness)
                                | (e.tmpStatus == Entity.Status.insider & currentEntity.aggressiveness < e.aggressiveness & !currentEntity.estrus & !e.estrus))) {
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
                        if (closestEnemy1.tmpStatus == Entity.Status.alien & !(closestEnemy2.tmpStatus == Entity.Status.alien))
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
                                    & e.tmpCheckDist
                                    & e.estrus
                                    & e.tmpStatus == Entity.Status.insider) {
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
                                    & e.tmpCheckDist
                                    & currentEntity.aggressiveness > e.aggressiveness
                                    & e.tmpStatus == Entity.Status.alien) {
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
                                    & e.tmpStatus == Entity.Status.insider
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
                                            deathByKillingAttacking++;
                                        entities.remove(indexClosestFood);
                                        if (indexClosestFood < currentEntityIndex)
                                            currentEntityIndex--;
                                    } else {
                                        closestFood.fullness += currentEntity.fullness * 0.5f;
                                        if (currentEntity.alive)
                                            deathByKillingDefender++;
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
                    currentEntity.fullness -= getCost(currentEntity.aggressiveness) * coefFullnessChange;
                    currentEntity.fullness -= getCost(currentEntity.force) * coefFullnessChange;
                    currentEntity.fullness -= getCost(currentEntity.immunity) * coefFullnessChange;
                    currentEntity.fullness -= getCost(currentEntity.maxSpeed) * coefFullnessChange;
                    currentEntity.fullness -= getCost(currentEntity.recoverySpeed) * coefFullnessChange;
                    currentEntity.fullness -= getCost(currentEntity.maxHealth) * coefFullnessChange;
                    currentEntity.fullness -= getCost(currentEntity.recoveryHealth) * coefFullnessChange;
                    currentEntity.fullness -= getCost(currentEntity.maxAge) * coefFullnessChange;
                    if (currentEntity.fullness <= minFullness) {
                        dead(currentEntity);
                        deathFromExhaustion++;
                        continue;
                    }
                } else
                    countDead++;
            }
        }

        private float getCost(float par) {
            return (float) (1 / Math.sqrt(1 - par * par) - 1);
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
                newEntity.aggressiveness = getHybrid(currentEntity.aggressiveness, currentEntity.malePartner.aggressiveness);
                newEntity.force = getHybrid(currentEntity.force, currentEntity.malePartner.force);
                newEntity.immunity = getHybrid(currentEntity.immunity, currentEntity.malePartner.immunity);
                newEntity.maxSpeed = getHybrid(currentEntity.maxSpeed, currentEntity.malePartner.maxSpeed);
                newEntity.recoverySpeed = getHybrid(currentEntity.recoverySpeed, currentEntity.malePartner.recoverySpeed);
                newEntity.maxHealth = getHybrid(currentEntity.maxHealth, currentEntity.malePartner.maxHealth);
                newEntity.recoveryHealth = getHybrid(currentEntity.recoveryHealth, currentEntity.malePartner.recoveryHealth);
                newEntity.maxAge = getHybrid(currentEntity.maxAge, currentEntity.malePartner.maxAge);
                sexualReproduction++;
            }
            if (Math.random() < chanceMutation)
                switch ((int) Math.ceil(Math.random() * countParam)) {
                    case 1:
                        newEntity.aggressiveness = getMutation(newEntity.aggressiveness);
                        break;
                    case 2:
                        newEntity.force = getMutation(newEntity.force);
                        break;
                    case 3:
                        newEntity.immunity = getMutation(newEntity.immunity);
                        break;
                    case 4:
                        newEntity.maxSpeed = getMutation(newEntity.maxSpeed);
                        break;
                    case 5:
                        newEntity.recoverySpeed = getMutation(newEntity.recoverySpeed);
                        break;
                    case 6:
                        newEntity.maxHealth = getMutation(newEntity.maxHealth);
                        break;
                    case 7:
                        newEntity.recoveryHealth = getMutation(newEntity.recoveryHealth);
                        break;
                    case 8:
                        newEntity.maxAge = getMutation(newEntity.maxAge);
                        break;
                }
            newEntity.currentSpeed = newEntity.maxSpeed;
            newEntity.currentHealth = newEntity.maxHealth;
            entities.add(newEntity);
        }

        private float getHybrid(float par1, float par2) {
            return (float) Math.sqrt(1f - Math.pow(1f / (((1f / Math.sqrt(1f - par1 * par1) - 1f) + (1f / Math.sqrt(1f - par2 * par2) - 1f)) / 2f + 1f), 2f));
        }

        private float getMutation(float par) {
            float coef = (float) new Random().nextGaussian();
            if (coef >= 0)
                return (float) Math.sqrt(1f - Math.pow(1f / ((1f / Math.sqrt(1f - par * par) - 1f) * (1f + coef) + 1f), 2f));
            else
                return (float) Math.sqrt(1f - Math.pow(1f / ((1f / Math.sqrt(1f - par * par) - 1f) / (1f - coef) + 1f), 2f));
        }

        private boolean twin(Entity currentEntity, Entity e) {
            float costE = getCost(e.aggressiveness);
            float costCur = getCost(currentEntity.aggressiveness);
            if (Math.max(costE, costCur) / Math.min(costE, costCur) > 2f)
                return false;
            costE = getCost(e.force);
            costCur = getCost(currentEntity.force);
            if (Math.max(costE, costCur) / Math.min(costE, costCur) > 2f)
                return false;
            costE = getCost(e.immunity);
            costCur = getCost(currentEntity.immunity);
            if (Math.max(costE, costCur) / Math.min(costE, costCur) > 2f)
                return false;
            costE = getCost(e.maxSpeed);
            costCur = getCost(currentEntity.maxSpeed);
            if (Math.max(costE, costCur) / Math.min(costE, costCur) > 2f)
                return false;
            costE = getCost(e.recoverySpeed);
            costCur = getCost(currentEntity.recoverySpeed);
            if (Math.max(costE, costCur) / Math.min(costE, costCur) > 2f)
                return false;
            costE = getCost(e.maxHealth);
            costCur = getCost(currentEntity.maxHealth);
            if (Math.max(costE, costCur) / Math.min(costE, costCur) > 2f)
                return false;
            costE = getCost(e.recoveryHealth);
            costCur = getCost(currentEntity.recoveryHealth);
            if (Math.max(costE, costCur) / Math.min(costE, costCur) > 2f)
                return false;
            costE = getCost(e.maxAge);
            costCur = getCost(currentEntity.maxAge);
            if (Math.max(costE, costCur) / Math.min(costE, costCur) > 2f)
                return false;
            return true;
        }

        private boolean alien(Entity currentEntity, Entity e) {
            float costE = getCost(e.aggressiveness);
            float costCur = getCost(currentEntity.aggressiveness);
            if (Math.max(costE, costCur) / Math.min(costE, costCur) > 3f)
                return true;
            costE = getCost(e.force);
            costCur = getCost(currentEntity.force);
            if (Math.max(costE, costCur) / Math.min(costE, costCur) > 3f)
                return true;
            costE = getCost(e.immunity);
            costCur = getCost(currentEntity.immunity);
            if (Math.max(costE, costCur) / Math.min(costE, costCur) > 3f)
                return true;
            costE = getCost(e.maxSpeed);
            costCur = getCost(currentEntity.maxSpeed);
            if (Math.max(costE, costCur) / Math.min(costE, costCur) > 3f)
                return true;
            costE = getCost(e.recoverySpeed);
            costCur = getCost(currentEntity.recoverySpeed);
            if (Math.max(costE, costCur) / Math.min(costE, costCur) > 3f)
                return true;
            costE = getCost(e.maxHealth);
            costCur = getCost(currentEntity.maxHealth);
            if (Math.max(costE, costCur) / Math.min(costE, costCur) > 3f)
                return true;
            costE = getCost(e.recoveryHealth);
            costCur = getCost(currentEntity.recoveryHealth);
            if (Math.max(costE, costCur) / Math.min(costE, costCur) > 3f)
                return true;
            costE = getCost(e.maxAge);
            costCur = getCost(currentEntity.maxAge);
            if (Math.max(costE, costCur) / Math.min(costE, costCur) > 3f)
                return true;
            return false;
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