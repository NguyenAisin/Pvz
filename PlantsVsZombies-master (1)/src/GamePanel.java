import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Random;
import java.net.URL;

/**
 * Created by Armin on 6/25/2016.
 */
public class GamePanel extends JLayeredPane implements MouseMotionListener {

    
    private JButton shovelBtn;
    private Image bgImage;
    private Image peashooterImage;
    private Image freezePeashooterImage;
    private Image sunflowerImage;
    private Image peaImage;
    private Image freezePeaImage;
    private Image shovelImage;

    private Image normalZombieImage;
    private Image coneHeadZombieImage;
    private Collider[] colliders;

    private ArrayList<ArrayList<Zombie>> laneZombies;
    private ArrayList<ArrayList<Pea>> lanePeas;
    private ArrayList<Sun> activeSuns;

    private Timer redrawTimer;
    private Timer advancerTimer;
    private Timer sunProducer;
    private Timer zombieProducer;
    
    
    private boolean isPaused = false;
    private Font pauseFont = new Font("Arial", Font.BOLD, 60);
    private boolean gameOver = false;
    
    private int sunScore;
    private JLabel sunScoreboard;
    
    private Timer gameTimer;
    private JProgressBar timeBar;
    private final int LEVEL_TIME = 60000;
    private JProgressBar progressBar;
    private JLabel levelLabel;
    
    
    private int btnWidth = 100;
    private int btnHeight = 30;
    private int margin = 10;

    private GameWindow.PlantType activePlantingBrush = GameWindow.PlantType.None;

    private int mouseX, mouseY;

    

    public int getSunScore() {
        return sunScore;
    }

    public void setSunScore(int sunScore) {
        this.sunScore = sunScore;
        sunScoreboard.setText(String.valueOf(sunScore));
    }

    public GamePanel(JLabel sunScoreboard) {
        setSize(1000, 752);
        setLayout(null);
        
        
        
        //them
        progressBar = new JProgressBar(0, getLevelTarget()); 
        progressBar.setValue(0);
        progressBar.setBounds(585, 10, 250, 25);
        progressBar.setStringPainted(true);
        add(progressBar);
        progressBar.setForeground(new Color(0, 200, 0)); // màu thanh
        progressBar.setBackground(new Color(40, 40, 40)); // nền trong khung

        progressBar.setFont(new Font("Arial", Font.BOLD, 14));

        progressBar.setBorderPainted(true);
        progressBar.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        progressBar.setOpaque(true);

        progressBar.setUI(new javax.swing.plaf.basic.BasicProgressBarUI() {
            @Override
            protected Color getSelectionForeground() {
                return Color.WHITE;
            }

            @Override
            protected Color getSelectionBackground() {
                return Color.WHITE;
            }
        });
        progress = 0;
        progressBar.setValue(0);
        
        //nhan input tu chuot
        addMouseMotionListener(this);
        
        //Nut pause cua game
        JButton btnPause = new JButton("Pause");
        
        btnPause.setForeground(new Color(102, 255, 102)); //xanh la
        btnPause.setBounds(1000 - btnWidth - margin - 15, 1, btnWidth, btnHeight);
        
        //an di nut pause
        btnPause.setBorderPainted(false);
        btnPause.setContentAreaFilled(false);
        btnPause.setFocusPainted(false);
        btnPause.setOpaque(false);
        
        add(btnPause, new Integer(2));

        btnPause.addActionListener(e -> {
            setPaused(!isPaused);
            btnPause.setText(isPaused ? "Resume" : "Pause");
        });
        
        
        
        //them xeng
        shovelBtn = new JButton("Shovel");
        shovelBtn.setBounds(595, 50, 100, 50);
        shovelBtn.setBackground(new Color(100, 100, 100));
        shovelBtn.setForeground(Color.WHITE);
        shovelBtn.setFont(new Font("Arial", Font.BOLD, 16));
        
        shovelBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        shovelBtn.setFocusPainted(false);
        shovelBtn.setBorderPainted(false);
        
        add(shovelBtn, new Integer(2));

        shovelBtn.addActionListener(e -> {
            activePlantingBrush = GameWindow.PlantType.Shovel;
        });

        //hien thi man hien tai
        levelLabel = new JLabel();
        levelLabel.setBounds(450, 10, 120, 25);
        levelLabel.setForeground(Color.WHITE);
        levelLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(levelLabel);

        updateLevelLabel();
        
        this.sunScoreboard = sunScoreboard;
        setSunScore(150);  //pool avalie

        bgImage = new ImageIcon(this.getClass().getResource("/images/mainBG.png")).getImage();

        peashooterImage = new ImageIcon(this.getClass().getResource("/images/plants/peashooter.gif")).getImage();
        freezePeashooterImage = new ImageIcon(this.getClass().getResource("/images/plants/freezepeashooter.gif")).getImage();
        sunflowerImage = new ImageIcon(this.getClass().getResource("/images/plants/sunflower.gif")).getImage();
        peaImage = new ImageIcon(this.getClass().getResource("/images/pea.png")).getImage();
        freezePeaImage = new ImageIcon(this.getClass().getResource("/images/freezepea.png")).getImage();
        
        normalZombieImage = new ImageIcon(this.getClass().getResource("/images/zombies/zombie1.png")).getImage();
        coneHeadZombieImage = new ImageIcon(this.getClass().getResource("/images/zombies/zombie2.png")).getImage();

        laneZombies = new ArrayList<>();
        laneZombies.add(new ArrayList<>()); //line 1
        laneZombies.add(new ArrayList<>()); //line 2
        laneZombies.add(new ArrayList<>()); //line 3
        laneZombies.add(new ArrayList<>()); //line 4
        laneZombies.add(new ArrayList<>()); //line 5

        lanePeas = new ArrayList<>();
        lanePeas.add(new ArrayList<>()); //line 1
        lanePeas.add(new ArrayList<>()); //line 2
        lanePeas.add(new ArrayList<>()); //line 3
        lanePeas.add(new ArrayList<>()); //line 4
        lanePeas.add(new ArrayList<>()); //line 5

        colliders = new Collider[45];
        for (int i = 0; i < 45; i++) {
            Collider a = new Collider();
            a.setLocation(44 + (i % 9) * 100, 109 + (i / 9) * 120);
            a.setAction(new PlantActionListener((i % 9), (i / 9)));
            colliders[i] = a;
            add(a, new Integer(0));
        }

        //colliders[0].setPlant(new FreezePeashooter(this,0,0));
/*
        colliders[9].setPlant(new Peashooter(this,0,1));
        laneZombies.get(1).add(new NormalZombie(this,1));*/

        activeSuns = new ArrayList<>();

        redrawTimer = new Timer(25, (ActionEvent e) -> {
            repaint();
        });
        redrawTimer.start();

        advancerTimer = new Timer(60, (ActionEvent e) -> advance());
        advancerTimer.start();

        sunProducer = new Timer(5000, (ActionEvent e) -> {
            Random rnd = new Random();
            Sun sta = new Sun(this, rnd.nextInt(800) + 100, 0, rnd.nextInt(300) + 200);
            activeSuns.add(sta);
            add(sta, new Integer(1));
        });
        sunProducer.start();

        zombieProducer = new Timer(7000, (ActionEvent e) -> {
            Random rnd = new Random();

            updateLevelLabel();
            progressBar.setMaximum(getLevelTarget());

            String[] Level = LevelData.LEVEL_CONTENT[Integer.parseInt(LevelData.LEVEL_NUMBER) - 1];
            int[][] LevelValue = LevelData.LEVEL_VALUE[Integer.parseInt(LevelData.LEVEL_NUMBER) - 1];
            int l = rnd.nextInt(5);
            int t = rnd.nextInt(100);
            Zombie z = null;
            for (int i = 0; i < LevelValue.length; i++) {
                if (t >= LevelValue[i][0] && t <= LevelValue[i][1]) {
                    z = Zombie.getZombie(Level[i], GamePanel.this, l);
                }
            }
            laneZombies.get(l).add(z);
        });
        zombieProducer.start();
        
        
        
        

    }

    private void advance() {

        // Zombies + Peas theo lane
        for (int i = 0; i < 5; i++) {

            // Zombies
            for (int j = laneZombies.get(i).size() - 1; j >= 0; j--) {
                Zombie z = laneZombies.get(i).get(j);
                z.advance();

                if (z.getHealth() <= 0 || !z.isMoving()) {
                    laneZombies.get(i).remove(j);
                    addProgress(10);
                }
            }

            // Peas
            for (int j = lanePeas.get(i).size() - 1; j >= 0; j--) {
                Pea p = lanePeas.get(i).get(j);
                p.advance();

                if (p.getPosX() > 1000) {
                    lanePeas.get(i).remove(j);
                }
            }
        }

        //mat troi
        for (int i = 0; i < activeSuns.size(); i++) {
            activeSuns.get(i).advance();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bgImage, 0, 0, null);

        //Draw Plants
        for (int i = 0; i < 45; i++) {
            Collider c = colliders[i];
            if (c.assignedPlant != null) {
                Plant p = c.assignedPlant;
                if (p instanceof Peashooter) {
                    g.drawImage(peashooterImage, 60 + (i % 9) * 100, 129 + (i / 9) * 120, null);
                }
                if (p instanceof FreezePeashooter) {
                    g.drawImage(freezePeashooterImage, 60 + (i % 9) * 100, 129 + (i / 9) * 120, null);
                }
                if (p instanceof Sunflower) {
                    g.drawImage(sunflowerImage, 60 + (i % 9) * 100, 129 + (i / 9) * 120, null);
                }
            }
        }

        for (int i = 0; i < 5; i++) {
            for (Zombie z : laneZombies.get(i)) {
                if (z instanceof NormalZombie) {
                    g.drawImage(normalZombieImage, z.getPosX(), 109 + (i * 120), null);
                } else if (z instanceof ConeHeadZombie) {
                    g.drawImage(coneHeadZombieImage, z.getPosX(), 109 + (i * 120), null);
                }
            }

            for (int j = 0; j < lanePeas.get(i).size(); j++) {
                Pea pea = lanePeas.get(i).get(j);
                if (pea instanceof FreezePea) {
                    g.drawImage(freezePeaImage, pea.getPosX(), 130 + (i * 120), null);
                } else {
                    g.drawImage(peaImage, pea.getPosX(), 130 + (i * 120), null);
                }
            }

        }
        //them
        if (activePlantingBrush == GameWindow.PlantType.Shovel) {
            g.drawImage(shovelImage, mouseX - 40, mouseY - 40, 80, 80, null);
            shovelImage = new ImageIcon(
            this.getClass().getResource("/images/shovel.png")
            ).getImage();
        }

        //if(!"".equals(activePlantingBrush)){
        //System.out.println(activePlantingBrush);
            /*if(activePlantingBrush == GameWindow.PlantType.Sunflower) {
                g.drawImage(sunflowerImage,mouseX,mouseY,null);
            }*/

        //}
        
        //them: hieu ung cho man hinh pause
        if (isPaused) {
            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setFont(pauseFont);
            g2.setColor(new Color(102, 255, 102));

            String text = "PAUSED";

            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();

            int x = (getWidth() - textWidth) / 2;
            int y = (getHeight() - textHeight) / 2 + fm.getAscent();

            g2.drawString(text, x, y);
        }

    }
    
    

    private class PlantActionListener implements ActionListener {

        int x, y;

        public PlantActionListener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            
            
            //da chinh sua va them
            if (activePlantingBrush == GameWindow.PlantType.Shovel) {
                Collider c = colliders[x + y * 9];
                if (c.assignedPlant != null) {
                c.removePlant();
                }
                activePlantingBrush = GameWindow.PlantType.None;
                return;
            }
            
            if (activePlantingBrush == GameWindow.PlantType.Sunflower) {
                if (getSunScore() >= 50) {
                    colliders[x + y * 9].setPlant(new Sunflower(GamePanel.this, x, y));
                    setSunScore(getSunScore() - 50);
                }
            }
            if (activePlantingBrush == GameWindow.PlantType.Peashooter) {
                if (getSunScore() >= 100) {
                    colliders[x + y * 9].setPlant(new Peashooter(GamePanel.this, x, y));
                    setSunScore(getSunScore() - 100);
                }
            }

            if (activePlantingBrush == GameWindow.PlantType.FreezePeashooter) {
                if (getSunScore() >= 175) {
                    colliders[x + y * 9].setPlant(new FreezePeashooter(GamePanel.this, x, y));
                    setSunScore(getSunScore() - 175);
                }
            }
            activePlantingBrush = GameWindow.PlantType.None;
        }
    }
    
    public void gameOver() {
        if (gameOver) return;

        gameOver = true;

        redrawTimer.stop();
        advancerTimer.stop();
        sunProducer.stop();
        zombieProducer.stop();

        JOptionPane.showMessageDialog(this,
                "ZOMBIES ATE YOUR BRAIN!\nRestarting level");

        GameWindow.gw.dispose();
        GameWindow.gw = new GameWindow();
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        repaint();
    }

    static int progress = 0;

    public void addProgress(int num) {
        progress += num;
        progressBar.setValue(progress);

        if (progress >= getLevelTarget()) {

            stopGame();

            if ("1".equals(LevelData.LEVEL_NUMBER)) {

                JOptionPane.showMessageDialog(this,
                        "LEVEL 1 Completed !!!\nStarting LEVEL 2");

                LevelData.write("2");
                progress = 0;

                GameWindow.gw.dispose();
                GameWindow.gw = new GameWindow();

            } else if ("2".equals(LevelData.LEVEL_NUMBER)) {

                JOptionPane.showMessageDialog(this,
                        "LEVEL 2 Completed !!!\nYOU WIN 🎉");

                progress = 0;
                LevelData.write("1");

                GameWindow.gw.dispose();
                GameWindow.gw = new GameWindow();
            }
        }
    }

    public GameWindow.PlantType getActivePlantingBrush() {
        return activePlantingBrush;
    }

    public void setActivePlantingBrush(GameWindow.PlantType activePlantingBrush) {
        this.activePlantingBrush = activePlantingBrush;
    }

    public ArrayList<ArrayList<Zombie>> getLaneZombies() {
        return laneZombies;
    }

    public void setLaneZombies(ArrayList<ArrayList<Zombie>> laneZombies) {
        this.laneZombies = laneZombies;
    }

    public ArrayList<ArrayList<Pea>> getLanePeas() {
        return lanePeas;
    }

    public void setLanePeas(ArrayList<ArrayList<Pea>> lanePeas) {
        this.lanePeas = lanePeas;
    }

    public ArrayList<Sun> getActiveSuns() {
        return activeSuns;
    }

    public void setActiveSuns(ArrayList<Sun> activeSuns) {
        this.activeSuns = activeSuns;
    }

    public Collider[] getColliders() {
        return colliders;
    }

    public void setColliders(Collider[] colliders) {
        this.colliders = colliders;
    }
    
    //them
    public void setPaused(boolean paused) {
        isPaused = paused;

        if (paused) {
            redrawTimer.stop();
            advancerTimer.stop();
            sunProducer.stop();
            zombieProducer.stop();
        } else {
            redrawTimer.start();
            advancerTimer.start();
            sunProducer.start();
            zombieProducer.start();
        }
        repaint();
    }
    
    //them
    private void updateLevelLabel() {
        levelLabel.setText("LEVEL " + LevelData.LEVEL_NUMBER);
    }
    
    private int getLevelTarget() {
        if ("1".equals(LevelData.LEVEL_NUMBER)) {
            return 150;
        } else if ("2".equals(LevelData.LEVEL_NUMBER)) {
            return 250;
        }
        return 150;
    }
    public void stopGame() {
    if (redrawTimer != null) redrawTimer.stop();
    if (advancerTimer != null) advancerTimer.stop();
    if (zombieProducer != null) zombieProducer.stop();
    if (sunProducer != null) sunProducer.stop();
    }
}
