import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Main {
    private final int W = 800;
    private final int H = 800;
    private final Color BG = new Color(255, 255, 255, 255);
    private final Color BLUE = new Color(0, 0, 255, 130);
    private final Color RED = new Color(255, 0, 0, 130);
    private final Color GREEN = new Color(0, 255, 0, 130);
    private final Color BLACK = new Color(0, 0, 0, 130);
    private final Color[] COLORS = new Color[3];
    private ArrayList<Bacterium> bacteria = new ArrayList<>();
    private ArrayList<Food> food = new ArrayList<>();
    private ArrayList<Integer> graph0 = new ArrayList<Integer>();
    private ArrayList<Integer> graph1 = new ArrayList<Integer>();
    private ArrayList<Integer> graph2 = new ArrayList<Integer>();
    private final int FOOD_RADIUS = 5;
    private int milisecondsCount = 0;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            }
            COLORS[0] = BLUE;
            COLORS[1] = RED;
            COLORS[2] = BLACK;
            JFrame frame = new JFrame("Main");
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
            Timer timer = new Timer(1, e -> {
                repaint();
            });
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
            Food a = new Food((float) (Math.random() * (W - 100) + 50), (float) (Math.random() * (H - 100) + 50));
            food.add(a);
            if (milisecondsCount % 10000 == 0)
                bacteria.add(new Bacterium(0, (float) (Math.random() * (W - 100) + 50), (float) (Math.random() * (H - 100) + 50)));
            milisecondsCount++;
            drawScene(g);
            drawBacteriumAndFood(g);
            drawGraph(g);
        }

        private void drawGraph(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            if (bacteria.size() > 0) {
                for (int i = 0; i < graph0.size(); i++) {
                    g2.setColor(BLUE);
                    g2.fillRect(i, H - graph0.get(i) / 4 - 1, 1, graph0.get(i) / 4);
                    g2.setColor(RED);
                    g2.fillRect(i, H - graph1.get(i) / 4 - 250, 1, graph1.get(i) / 4);
                    g2.setColor(BLACK);
                    g2.fillRect(i, H - graph2.get(i) / 4 - 500, 1, graph2.get(i) / 4);
                }
                if ((milisecondsCount % 100) == 0) {
                    if(graph0.size()>800){
                        graph0.clear();
                        graph1.clear();
                        graph2.clear();
                    }
                    graph0.add((int) bacteria.stream().filter(a -> a.type == 0).count());
                    graph1.add((int) bacteria.stream().filter(a -> a.type == 1).count());
                    graph2.add((int) bacteria.stream().filter(a -> a.type == 2).count());
                }
            }
        }

        private void drawScene(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(BG);
            g2.fillRect(0, 0, W, H);
        }

        private void drawBacteriumAndFood(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (Food a : food) {
                g2.setColor(GREEN);
                g2.fillOval((int) a.x - FOOD_RADIUS, (int) a.y - FOOD_RADIUS, FOOD_RADIUS * 2, FOOD_RADIUS * 2);
            }
            for (Bacterium a : bacteria) {
                g2.setColor(COLORS[a.type]);
                g2.fillOval((int) a.x - a.radius, (int) a.y - a.radius, a.radius * 2, a.radius * 2);
            }
        }

        private void logic() {
            for (Bacterium a : bacteria) {
                double targetAngle = Math.atan2(a.ty, a.tx);
                a.x += (float) Math.cos(targetAngle) * a.speed + Math.random() - 0.5f;
                a.y += (float) Math.sin(targetAngle) * a.speed + Math.random() - 0.5f;
                if (a.x < 0) a.x += 1;
                else if (a.x > W) a.x -= 1;
                if (a.y < 0) a.y += 1;
                else if (a.y > H) a.y -= 1;
                if (a.type == 0) {
                    Food closestFood = null;
                    float minFoodDist = (W * W) + (H * H);
                    for (Food f : food) {
                        if (f.toBeDeleted) continue;
                        float distZeroFood = (a.x - f.x) * (a.x - f.x) + (a.y - f.y) * (a.y - f.y);
                        if (distZeroFood < a.sightDistance * a.sightDistance) {
                            if (distZeroFood < minFoodDist) {
                                minFoodDist = distZeroFood;
                                closestFood = f;
                            }
                        }
                    }
                    Bacterium closestEnemy = null;
                    float minEnemyDist = (W * W) + (H * H);
                    for (Bacterium b : bacteria) {
                        if (b.type == 0) continue;
                        if (b.speed < a.speed) continue;
                        float distZeroEnemy = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
                        if (distZeroEnemy < a.sightDistance * a.sightDistance) {
                            if (distZeroEnemy < minEnemyDist) {
                                minEnemyDist = distZeroEnemy;
                                closestEnemy = b;
                            }
                        }
                    }
                    if (closestEnemy != null) {
                        a.tx = -closestEnemy.x + a.x;
                        a.ty = -closestEnemy.y + a.y;
                    } else {
                        if (closestFood != null) {
                            a.tx = closestFood.x - a.x;
                            a.ty = closestFood.y - a.y;
                            if (minFoodDist < a.radius * a.radius) {
                                closestFood.toBeDeleted = true;
                                a.food++;
                            }
                        } else {
                            if (Math.random() < a.directionChangeRate) {
                                double randomAngle = Math.random() * Math.PI * 2;
                                a.tx = (float) Math.cos(randomAngle) * 2;
                                a.ty = (float) Math.sin(randomAngle) * 2;
                            }
                        }
                    }
                }
                if (a.type == 1) {
                    Bacterium closestFood = null;
                    float minFoodDist = (W * W) + (H * H);
                    for (Bacterium b : bacteria) {
                        if (b.toBeDeleted) continue;
                        if (b.type == 1) continue;
                        if (b.type == 2) continue;
                        if (b.speed > a.speed) continue;
                        float distOneZero = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
                        if (distOneZero < a.sightDistance * a.sightDistance) {
                            if (distOneZero < minFoodDist) {
                                minFoodDist = distOneZero;
                                closestFood = b;
                            }
                        }
                    }
                    Bacterium closestEnemy = null;
                    float minEnemyDist = (W * W) + (H * H);
                    for (Bacterium b : bacteria) {
                        if (b.type == 0) continue;
                        if (b.type == 1) continue;
                        if (b.speed < a.speed) continue;
                        float distZeroEnemy = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
                        if (distZeroEnemy < a.sightDistance * a.sightDistance) {
                            if (distZeroEnemy < minEnemyDist) {
                                minEnemyDist = distZeroEnemy;
                                closestEnemy = b;
                            }
                        }
                    }
                    if (closestEnemy != null) {
                        a.tx = -closestEnemy.x + a.x;
                        a.ty = -closestEnemy.y + a.y;
                    } else {
                        if (closestFood != null) {
                            a.tx = closestFood.x - a.x;
                            a.ty = closestFood.y - a.y;
                            if (minFoodDist < a.radius * a.radius) {
                                closestFood.toBeDeleted = true;
                                a.food += closestFood.food * 0.5f;
                            }
                        } else {
                            if (Math.random() < a.directionChangeRate) {
                                double randomAngle = Math.random() * Math.PI * 2;
                                a.tx = (float) Math.cos(randomAngle) * 2;
                                a.ty = (float) Math.sin(randomAngle) * 2;
                            }
                        }
                    }
                }
                if (a.type == 2) {
                    Bacterium closestFoodOne = null;
                    Bacterium closestFoodZero = null;
                    float minFoodDistOne = (W * W) + (H * H);
                    float minFoodDistZero = (W * W) + (H * H);
                    for (Bacterium b : bacteria) {
                        if (b.toBeDeleted) continue;
                        if (b.type == 2) continue;
                        if (b.speed > a.speed) continue;
                        float distTwoOne = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
                        float distTwoZero = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
                        if (b.type == 1) {
                            if (distTwoOne < a.sightDistance * a.sightDistance) {
                                if (distTwoOne < minFoodDistOne) {
                                    minFoodDistOne = distTwoOne;
                                    closestFoodOne = b;
                                }
                            }
                        } else {
                            if (distTwoZero < a.sightDistance * a.sightDistance) {
                                if (distTwoZero < minFoodDistZero) {
                                    minFoodDistZero = distTwoZero;
                                    closestFoodZero = b;
                                }
                            }
                        }
                    }
                    if (closestFoodOne != null) {
                        a.tx = closestFoodOne.x - a.x;
                        a.ty = closestFoodOne.y - a.y;
                        if (minFoodDistOne < a.radius * a.radius + a.radius * a.radius) {
                            closestFoodOne.toBeDeleted = true;
                            a.food += closestFoodOne.food * 0.5f;
                        }
                    } else if (closestFoodZero != null) {
                        a.tx = closestFoodZero.x - a.x;
                        a.ty = closestFoodZero.y - a.y;
                        if (minFoodDistZero < a.radius * a.radius + a.radius * a.radius) {
                            closestFoodZero.toBeDeleted = true;
                            a.food += closestFoodZero.food * 0.25f;
                        }
                    } else {
                        if (Math.random() < a.directionChangeRate) {
                            double randomAngle = Math.random() * Math.PI * 2;
                            a.tx = (float) Math.cos(randomAngle) * 2;
                            a.ty = (float) Math.sin(randomAngle) * 2;
                        }
                    }
                }
            }
            for (int i = 0; i < bacteria.size(); i++) {
                Bacterium a = bacteria.get(i);
                if (a.food >= 10) {
                    a.food -= 5;
                    Bacterium b = new Bacterium(a.type, a.x + (float) Math.random() * 10 - 5, a.y + (float) Math.random() * 10 - 5);
                    b.speed = a.speed;
                    b.radius = a.radius;
                    bacteria.add(b);
                }
                if (Math.random() < 0.00001) {
                    a.speed = a.speed * 1.1f;
                    if (a.radius > 6) a.radius--;
                    if (a.type == 0) {
                        a.type = 1;
                        continue;
                    }
                    if (a.type == 1) {
                        a.type = 2;
                        continue;
                    }
                    if (a.type == 2) {
                        a.type = 0;
                        continue;
                    }
                }
                if (a.food <= 0) a.toBeDeleted = true;
                else {
                    if (a.age % 500 == 499) a.food -= a.speed;
                    a.age++;
                }
                if (a.toBeDeleted) {
                    bacteria.remove(i);
                    i--;
                }
            }
            for (int i = 0; i < food.size(); i++) {
                if (food.get(i).toBeDeleted) {
                    food.remove(i);
                    i--;
                }
            }
        }
    }
}