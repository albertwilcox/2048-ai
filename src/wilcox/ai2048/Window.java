package wilcox.ai2048;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Window extends JFrame {
    HashMap<Integer, Image> images;

    Game game;
    AI ai;

    GamePanel gp;
    JPanel main;
    JButton reset, startAI, stepAI, stopAI;
    JSlider AISpeed, AIDepth;
    JLabel score, speed, depth;

    boolean AIRunning = false;

    public Window(Game g){
        super("2048 AI");
        this.game = g;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ai = new AI(this, g);

        images = new HashMap<>();
        loadImages();

        addKeyListener(new KeyListener() {
            HashMap<Integer, Boolean> pressed = new HashMap<>();

            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                try{
                    boolean already = pressed.get(key);
                    if (!already) {
                        handlePush(key);
                    }
                } catch (NullPointerException ex){
                    handlePush(key);
                }

            }

            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                pressed.put(key, false);
            }

            public void handlePush(int key){
                pressed.put(key, true);
                //System.out.println("here")
                if (AIRunning) {
                    System.out.println("Ignoring key because ai is running");
                    return;
                }
                switch(key){
                    case KeyEvent.VK_LEFT:
                        game.left();
                        break;
                    case KeyEvent.VK_RIGHT:
                        game.right();
                        break;
                    case KeyEvent.VK_UP:
                        game.up();
                        break;
                    case KeyEvent.VK_DOWN:
                        game.down();
                        break;
                }
                update();
            }
        });

        reset = new JButton("Reset Game");
        startAI = new JButton("Start AI");
        stepAI = new JButton("Step AI");
        stopAI = new JButton("Stop AI");
        reset.addActionListener(e -> {
            game = new Game();
            gp.setGame(game);
            stopAI.setEnabled(false);
            startAI.setEnabled(true);
            stepAI.setEnabled(true);
            AIDepth.setEnabled(true);
            AIRunning = false;
            ai = new AI(this, game);
            AISpeed.setValue(1000);
            update();
        });
        startAI.addActionListener(e -> {
            stopAI.setEnabled(true);
            startAI.setEnabled(false);
            stepAI.setEnabled(false);
            reset.setEnabled(false);
            AIDepth.setEnabled(false);
            AIRunning = true;
            ai.start();
                });
        stepAI.addActionListener(e -> {
            ai.act();
        });
        stopAI.addActionListener(e -> {
            stopAI.setEnabled(false);
            startAI.setEnabled(true);
            stepAI.setEnabled(true);
            reset.setEnabled(true);
            AIDepth.setEnabled(true);
            AIRunning = false;
            ai.stop();
        });
        stopAI.setEnabled(false);
        reset.setFocusable(false);
        startAI.setFocusable(false);
        stepAI.setFocusable(false);
        stopAI.setFocusable(false);

        score = new JLabel("Score: 0");
        speed = new JLabel("AI Speed:");
        depth = new JLabel("AI Search Depth:");
        AISpeed = new JSlider(0, 1000, 1000);
        AISpeed.addChangeListener(e -> {
            ai.setDelay(1010 - AISpeed.getValue());
        });
        AISpeed.setPreferredSize(new Dimension(100, 5));
        AISpeed.setFocusable(false);
        AIDepth = new JSlider(1, 4, 3);
        AIDepth.addChangeListener(e -> {
            ai.setSearchDepth(AIDepth.getValue());
        });
        AIDepth.setPreferredSize(new Dimension(100, 5));
        AIDepth.setPaintTicks(true);
        AIDepth.setMajorTickSpacing(1);
        AIDepth.setPaintLabels(true);
        AIDepth.setFocusable(false);

        gp = new GamePanel(game);
        main  = new JPanel();
        GroupLayout layout = new GroupLayout(main);
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup()
                .addComponent(gp)
                .addGap(335))
            .addGroup(layout.createParallelGroup()
                .addComponent(score)
                .addComponent(reset)
                .addComponent(startAI)
                .addComponent(stepAI)
                .addComponent(stopAI)
                .addComponent(speed)
                .addComponent(AISpeed)
                .addComponent(depth)
                .addComponent(AIDepth)));
        layout.setVerticalGroup(layout.createParallelGroup()
            .addComponent(gp)
            .addGroup(layout.createSequentialGroup()
                .addComponent(score)
                .addGap(30)
                .addComponent(reset)
                .addGap(30)
                .addComponent(startAI)
                .addComponent(stepAI)
                .addComponent(stopAI)
                .addComponent(speed)
                .addComponent(AISpeed)
                .addComponent(depth)
                .addComponent(AIDepth)));
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        main.setLayout(layout);

        add(main);
        setVisible(true);
        setSize(485, 396);
        setResizable(false);
        //pack();
    }

    private void loadImages(){
        int[] list = new int[17];
        for (int i = 1; i <= 17; i++){
            list[i-1] = (int) Math.pow(2.0, (double)i);
        }
        for (int x: list){
            try {
                Image im = ImageIO.read(new File("lib/" + x + ".png"));
                images.put(x, im);
            } catch (IOException e){
                System.out.println(x);
                e.printStackTrace();
            }
        }
        try{
            Image im = ImageIO.read(new File("lib/empty.png"));
            images.put(0, im);
        } catch (IOException e){e.printStackTrace();}
    }

    /**
     * Terribly named function the AI will call when it's stuck
     */
    public void stuck(){
        ai.stop();
        startAI.setEnabled(false);
        stopAI.setEnabled(false);
        reset.setEnabled(true);
        AIRunning = false;
    }

    public void update(){
        gp.repaint();
        score.setText("Score: " + game.getScore());
        // Tester code---------------------------------------
        System.out.println(AI.desirability(game));
        //Game g2 = game.clone();
        //g2.left();
        //System.out.println(g2.testString());
        //System.out.println(AI.desirability(g2));

//        System.out.println(game.getScore());
    }

    private class GamePanel extends JPanel{
        Game game;

        private static final int SEPARATION = 7, SQUARE_SIZE = 75;
        private static final int SIZE = SEPARATION * 5 + SQUARE_SIZE * 4 * 100;
        Color background;

        public GamePanel(Game game){
            this.game = game;
            this.background = new Color(240, 240, 240);
            setSize(SIZE, SIZE);
        }

        @Override
        public void paintComponent(Graphics g){
            g.setColor(background);
            g.fillRect(0, 0, SIZE, SIZE);

            Tile[][] board = game.getBoard();

            for (int row = 0; row < 4; row++){
                int y = SEPARATION * (row + 1) + SQUARE_SIZE * row;
                for (int col = 0; col < 4; col++){
                    int x = SEPARATION * (col + 1) + SQUARE_SIZE * col;
                    g.drawImage(images.get(board[row][col].getValue()), x, y, null);
                }
            }
        }

        public void setGame(Game game){
            this.game = game;
        }
    }
}
