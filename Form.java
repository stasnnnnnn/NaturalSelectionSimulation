import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class Form extends JFrame implements Runnable {

    private final int w = 1000;
    private final int h = 1000;
    private final int FRAMES_TOTAL = 500000;
    private final Color BG = new Color(255, 255, 255, 255);
    private final Color BLUE = new Color(0, 0, 255, 130);
    private final Color RED = new Color(255, 0, 0, 130);
	private final Color GREEN = new Color(0, 255, 0, 255);
	private final Color BLACK = new Color(0, 0, 0, 130);
    private BufferedImage buf = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    private BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    private BufferedImage graph = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    private Color Colors[] = new Color[3];
    private ArrayList<Bacterium> bacteria = new ArrayList<>();
    private ArrayList<Food> food = new ArrayList<>();
    private final int FOOD_RADIUS = 5;
    private int frame = 0;

    public Form() {
		Colors[0]=BLUE;
		Colors[1]=RED;
		Colors[2]=BLACK;
        this.setSize(w + 16, h + 38);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocation(50, 50);
    }

    @Override
    public void run() {
        while(frame < FRAMES_TOTAL) this.repaint();        
    }

    @Override
    public void paint(Graphics g) {
		logic();
		if(frame % 1 == 0) {
            Food a = new Food((float)(Math.random() * (w - 100) + 50), (float)(Math.random() * (h - 100) + 50));
            food.add(a);
        }
		if(frame % 10000 == 0) bacteria.add(new Bacterium(0, (float)(Math.random() * (w - 100) + 50), (float)(Math.random() * (h - 100) + 50)));        
        frame++;
		try {
			drawScene(img);
			drawGraph(graph);
			drawBacteriumAndFood(img);
		} catch (IOException e) {
			e.printStackTrace();
		}			
		Graphics2D g2 = buf.createGraphics();
		g2.drawImage(img, null, 0, 0);
		g2.drawImage(graph, null, 0, 0);
		((Graphics2D)g).drawImage(buf, null, 8, 30);		
	}

    private void drawGraph(BufferedImage image) throws IOException {
        Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if(bacteria.size() > 0) {
            int type0 = (int)bacteria.stream().filter(a -> a.type == 0).count();
            int type1 = (int)bacteria.stream().filter(a -> a.type == 1).count();
			int type2 = (int)bacteria.stream().filter(a -> a.type == 2).count();
            int py = type0;
            if (py > h - 1) py = h - 1;
            int py1 = type1;
            if (py1 > h - 1) py1 = h - 1;
			int py2 = type2;
            if (py2 > h - 1) py2 = h - 1;
			if((frame % 1000) == 0){
				int px = (int) ((float) frame / FRAMES_TOTAL * (w - 1));
				g2.setColor(BLUE);
				g2.fillRect(px, h - py/4 - 1, 1, py/4);
				g2.setColor(RED);
				g2.fillRect(px, h - py1/4 - 333, 1, py1/4);
				g2.setColor(BLACK);
				g2.fillRect(px, h - py2/4 - 666, 1, py2/4);
			}
		}
    }

    private void drawScene(BufferedImage image) throws IOException {
        Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(BG);
        g2.fillRect(0, 0, w, h);
    }
	
	private void drawBacteriumAndFood(BufferedImage image) throws IOException {
		Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Food a : food) {
            g2.setColor(GREEN);
            g2.fillOval((int) a.x - FOOD_RADIUS, (int) a.y - FOOD_RADIUS, FOOD_RADIUS * 2, FOOD_RADIUS * 2);
        }        
        for (Bacterium a : bacteria) {
     		g2.setColor(Colors[a.type]);
            g2.fillOval((int) a.x - a.radius, (int) a.y - a.radius, a.radius * 2, a.radius * 2);			
        }
    }

    private void logic() {
        for (Bacterium a : bacteria) {            
			double targetAngle = Math.atan2(a.ty, a.tx);
            a.x += (float)Math.cos(targetAngle) * a.speed + Math.random() - 0.5f;
            a.y += (float)Math.sin(targetAngle) * a.speed + Math.random() - 0.5f;
            if(a.x < 0) a.x += 1;            
            else if(a.x > w) a.x -= 1;            
            if(a.y < 0) a.y += 1;
            else if(a.y > h) a.y -= 1;
			if (a.type == 0) {
				Food closestFood = null;
                float minFoodDist = (w * w) + (h * h);
                for (Food f : food) {
                    if (f.toBeDeleted) continue;
                    float distZeroFood = (a.x - f.x) * (a.x - f.x) + (a.y - f.y) * (a.y - f.y);
                    if(distZeroFood < a.sightDistance * a.sightDistance) {
                        if (distZeroFood < minFoodDist) {
                            minFoodDist = distZeroFood;
                            closestFood = f;
                        }
                    }
                }
			    Bacterium closestEnemy = null;
				float minEnemyDist = (w * w) + (h * h);
				for (Bacterium b : bacteria) {
                    if (b.type == 0) continue;
					if (b.speed < a.speed) continue;						
                    float distZeroEnemy = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
                    if(distZeroEnemy < a.sightDistance * a.sightDistance) {
                        if (distZeroEnemy < minEnemyDist) {
                            minEnemyDist = distZeroEnemy;
                            closestEnemy = b;
                        }
                    }
                }
				if (closestEnemy != null) {						
                    a.tx = -closestEnemy.x + a.x;
                    a.ty = -closestEnemy.y + a.y;
				}
				else{                    
					if (closestFood != null) {
                        a.tx = closestFood.x - a.x;
                        a.ty = closestFood.y - a.y;
                        if (minFoodDist < a.radius * a.radius) {
                            closestFood.toBeDeleted = true;
                            a.food++;
                        }
                    }
                    else{
                        if(Math.random() < a.directionChangeRate) {
                            double randomAngle = Math.random() * Math.PI * 2;
                            a.tx = (float)Math.cos(randomAngle) * 2;
                            a.ty = (float)Math.sin(randomAngle) * 2;
						}
					}
				}
            }
			if (a.type == 1) {
                Bacterium closestFood = null;
                float minFoodDist = (w * w) + (h * h);
                for (Bacterium b : bacteria) {
                    if (b.toBeDeleted) continue;
                    if (b.type == 1) continue;
				    if (b.type == 2) continue;
					if (b.speed > a.speed) continue;
                    float distOneZero = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
                    if(distOneZero < a.sightDistance * a.sightDistance) {
                        if (distOneZero < minFoodDist) {
                            minFoodDist = distOneZero;
                            closestFood = b;
                        }
                    }
                }
				Bacterium closestEnemy = null;
				float minEnemyDist = (w * w) + (h * h);
				for (Bacterium b : bacteria) {
                    if (b.type == 0) continue;
					if (b.type == 1) continue;	
					if (b.speed < a.speed) continue;						
                    float distZeroEnemy = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
                    if(distZeroEnemy < a.sightDistance * a.sightDistance) {
                        if (distZeroEnemy < minEnemyDist) {
                            minEnemyDist = distZeroEnemy;
                            closestEnemy = b;
                        }
                    }
                }
				if (closestEnemy != null) {						
					a.tx = -closestEnemy.x + a.x;
					a.ty = -closestEnemy.y + a.y;
				}
				else{                  
					if (closestFood != null) {
						a.tx = closestFood.x - a.x;
						a.ty = closestFood.y - a.y;
						if (minFoodDist < a.radius * a.radius) {
							closestFood.toBeDeleted = true;
							a.food += closestFood.food * 0.5f;
						}
					}
					else{
						if(Math.random() < a.directionChangeRate) {
							double randomAngle = Math.random() * Math.PI * 2;
							a.tx = (float)Math.cos(randomAngle) * 2;
							a.ty = (float)Math.sin(randomAngle) * 2;
						}
					}
				}
			}
			if (a.type == 2) {
				Bacterium closestFoodOne = null;
				Bacterium closestFoodZero = null;
				float minFoodDistOne = (w * w) + (h * h);
				float minFoodDistZero = (w * w) + (h * h);
				for (Bacterium b : bacteria) {
					if (b.toBeDeleted) continue;
					if (b.type == 2) continue;
					if (b.speed > a.speed) continue;
					float distTwoOne = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
					float distTwoZero = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
					if(b.type == 1){
						if(distTwoOne < a.sightDistance * a.sightDistance) {
							if (distTwoOne < minFoodDistOne) {
								minFoodDistOne = distTwoOne;
								closestFoodOne = b;
							}
						}
					}
					else{
						if(distTwoZero < a.sightDistance * a.sightDistance) {
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
				}
				else if (closestFoodZero != null) {
					a.tx = closestFoodZero.x - a.x;
					a.ty = closestFoodZero.y - a.y;
					if (minFoodDistZero < a.radius * a.radius + a.radius * a.radius) {
						closestFoodZero.toBeDeleted = true;
						a.food += closestFoodZero.food * 0.25f;
					}
				}
				else{
					if(Math.random() < a.directionChangeRate) {
						double randomAngle = Math.random() * Math.PI * 2;
						a.tx = (float)Math.cos(randomAngle) * 2;
						a.ty = (float)Math.sin(randomAngle) * 2;
					}
				}
			}
		}
        for (int i = 0; i < bacteria.size(); i++) {
            Bacterium a = bacteria.get(i);
            if(a.food >= 10) {
                a.food -= 5;
                int type = a.type;
                Bacterium b = new Bacterium(a.type, a.x + (float)Math.random() * 10 - 5, a.y + (float)Math.random() * 10 - 5);
                b.speed = a.speed;
				b.radius = a.radius;
                bacteria.add(b);
            }
			if(Math.random() < 0.00001) {
				a.speed = a.speed * 1.1f;
				if(a.radius > 6) a.radius--;
				if(a.type == 0) {
					a.type = 1;
					continue;
				}
				if(a.type == 1) {
					a.type = 2;					
					continue;
				}
				if(a.type == 2) {
					a.type = 0;					
					continue;
				}
            }
            if(a.food <= 0) a.toBeDeleted = true;
            else {
                if(a.age % 500 == 499) a.food -= a.speed;
                a.age++;
            }
            if(a.toBeDeleted) {
                bacteria.remove(i);
                i--;
            }			
        }
        for (int i = 0; i < food.size(); i++) {
            if(food.get(i).toBeDeleted) {
                food.remove(i);
                i--;
            }
        }
    }
}