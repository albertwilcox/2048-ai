package wilcox.ai2048;

import java.util.ArrayList;

public class AI {

    private static final int LEFT = 1, RIGHT = 0, UP = 3, DOWN = 2;

    private Window window;
    private Game game;

    private boolean running = false;
    int delay = 10, searchDepth = 3;

    private static boolean DEBUG_PRINTING = false;

    public AI(Window window, Game game){
        this.window = window;
        this.game = game;
    }

    public void start(){
        running = true;
        Thread t = new Thread(){
            @Override
            public void run(){
                //boolean direction = true;
                while (running){
                    long end = System.currentTimeMillis() + delay;
                    // Tester AI -------------------
//                    if (direction) {
//                        if (game.canLeft()) game.left();
//                        else if (game.canDown()) game.down();
//                        else if (game.canRight()) game.right();
//                        else if (game.canUp()) game.up();
//                        else window.stuck();
//                    } else{
//                        if (game.canDown()) game.down();
//                        else if (game.canLeft()) game.left();
//                        else if (game.canRight()) game.right();
//                        else if (game.canUp()) game.up();
//                        else window.stuck();
//                    }
//                    direction = !direction;
//                    if (game.isGridLocked()) window.stuck();
//                    int dir = findBestMove(game);
//                    switch (dir){
//                        case LEFT:
//                            game.left();
//                            //if (DEBUG_PRINTING) System.out.println("left");
//                            break;
//                        case RIGHT:
//                            game.right();
//                            //if (DEBUG_PRINTING) System.out.println("right");
//                            break;
//                        case UP:
//                            game.up();
//                            //if (DEBUG_PRINTING) System.out.println("up");
//                            break;
//                        case DOWN:
//                            game.down();
//                            //if (DEBUG_PRINTING) System.out.println("down");
//                            break;
//                    }
                    // End Tester AI ---------------
                    act();
                    try{
                        Thread.sleep(Math.max(end - System.currentTimeMillis(), 0));
                    } catch (InterruptedException e){e.printStackTrace();}
                }
            }
        };
        t.start();
    }

    public void act(){
        if (DEBUG_PRINTING) System.out.println("----------------------------------------");
        GameState gs = new GameState(game, 1);
        if (game.isGridLocked()) window.stuck();
        long[] out = gs.findBestMove();
        int dir = (int) out[0];
        //if (DEBUG_PRINTING) System.out.println(out[1]);
        switch (dir){
            case LEFT:
                game.left();
                if (DEBUG_PRINTING) System.out.println("left");
                break;
            case RIGHT:
                game.right();
                if (DEBUG_PRINTING) System.out.println("right");
                break;
            case UP:
                game.up();
                //stop();
                if (DEBUG_PRINTING) System.out.println("up");
                break;
            case DOWN:
                game.down();
                if (DEBUG_PRINTING) System.out.println("down");
                break;
        }
        window.update();
    }

    /**
     * Returns the weighted average of the shallowDesirability indexes of the gamestates in list
     * @param list
     * @return
     */
    public static long weightedAverage(ArrayList<GameState> list){
        double total = 0;
        for (GameState gs: list){
            long desirability = desirability(gs.getGame());
            total += desirability * gs.getProbability();
        }
        return (int)total;
    }

    /**
     * Takes in a game state and returns a shallowDesirability index for that game state
     * @param g game object representing the game state
     * @return
     */
    public static long desirability(Game g){
        if (g.isGridLocked()) return 1;

        /*
         * This rewards gamestates with longer chains of descending tiles
         */
        double snakeSize = 1, snakeLength = 1;
        int x1 = 1, x2 = 2, y1 = 3, y2 = 3, direction = 1;
        Tile[][] board = g.getBoard();
 //       boolean bestOccupied = false;
        if (board[3][0].getValue() >= board[3][1].getValue() && board[3][0].getValue() != 0){
            snakeSize += board[3][0].getValue();
            //bestOccupied = true;
        }
        while(y2 > 0 && board[y1][x1].getValue() >= board[y2][x2].getValue() && board[y1][x1].getValue() != 0){
            snakeSize += board[y1][x1].getValue();
            snakeLength ++;
            x1 = x2; y1 = y2;
            x2 += direction;
            if (x2 > 3 || x2 < 0){
                x2 -= direction;
                y2 -= 1;
                direction *= -1;
            }
        }
        boolean debug = false;
        if (DEBUG_PRINTING || debug) System.out.println("\n(" + x1 + ", " + y1 + ")");
        snakeSize = snakeSize * Math.log(snakeSize);// / Math.log(2);
        snakeSize *= Math.sqrt(snakeLength);

        int bottomCornerMultiplier;
//        if (bestOccupied){
        bottomCornerMultiplier = (int)(Math.log(board[3][0].getValue()) / Math.log(2));
        if (bottomCornerMultiplier < 0) bottomCornerMultiplier = 1;
//        } else {
//            bottomCornerMultiplier = (int)(Math.log(board[3][1].getValue()) / Math.log(2));
//        }

        /*
         * This rewards gamestates with higher tiles
         */
        int tileMultiplier = (int)(Math.log(g.maxTile()) / Math.log(2));

        /*
         * This rewards gamestates with more empty tiles
         */
        double emptyMultiplier = 1 + .1 * g.getEmptyTiles().size();

        if (DEBUG_PRINTING || debug) System.out.println("Snake: " + snakeSize + "\nTile: " + tileMultiplier + "\nEmpties: " + emptyMultiplier + "\nCorner: " + bottomCornerMultiplier);

        return (int) (Math.sqrt(snakeSize) * tileMultiplier  * Math.max(Math.log(g.getScore()), 0) * emptyMultiplier * bottomCornerMultiplier) + 2;
    }

    public void stop(){
        running = false;
    }

    public void setDelay(int delay){
        this.delay = delay;
    }

    public void setSearchDepth(int depth){
        this.searchDepth = depth;
    }

    private static void printList(long[] ls){
        String s = "[";
        for (long x: ls){
            s += x + ", ";
        }
        if (DEBUG_PRINTING) System.out.println(s + "]");
    }

    private class GameState{
        Game game;
        double probability;
        int level;
        long desirability;

        //static int count = 0;

        public GameState(Game game, double probability, int level){
            this.game = game;
            this.probability = probability;
            this.level = level;
            this.desirability = desirability(game);
        }

        public GameState(Game game, double probability){
            this(game, probability, 1);
        }

        /**
         * Finds and returns a list whose 0 element is the best move and 1 element is the expected score at level 5
         * @return
         */
        public long[] findBestMove(){
            /*
             * Creates game state clouds for each possible direction
             */
            GameStateCloud[] gscs = new GameStateCloud[4];
            long[] desirabilities = new long[4];
            for (int i = 0; i < 4; i++){
                gscs[i] = generateCloud(game, i);
                desirabilities[i] = gscs[i].getShallowDesirability();
            }
            if (level == 1) printList(desirabilities);


//            String s = "";
//            while (s.length() < level) s += " ";
//            if (DEBUG_PRINTING) System.out.print(s);
//            printList(desirabilities);

            //if (DEBUG_PRINTING) System.out.println(++count);

            /*
             * Base case
             */
            if (level == searchDepth){
                long[] out = {0, 0};
                for (int i = 0; i < 4; i++){
                    if (desirabilities[i] > out[1]){
                        out[0] = i;
                        out[1] = desirabilities[i];
                    }
                }
                return out;
            }

            /*
             * Check if any have a far better outcome
             */
            int maxIndex = 0;
            for (int i = 0; i < 4; i++)
                if (desirabilities[i] > desirabilities[maxIndex]) maxIndex = i;
            long shrunk = desirabilities[maxIndex] * 3 / 4;
            for (int i = 0; i < 4; i++)
                if (desirabilities[i] < shrunk)
                    desirabilities[i] = (desirabilities[i] > 0) ? 1 : 0;

            /*
             * Recursion
             */
            long[] deepDesirabilities = new long[4];
            for (int i = 0; i < 4; i++){
                // If statement to weed out clouds with very low shallow desirability
                if (desirabilities[i] > desirability / 2){
                    deepDesirabilities[i] = gscs[i].getDeepDesirability();
                } else {
//                    if (level == 1){
//                        if (DEBUG_PRINTING) System.out.print("---");
//                        printList(deepDesirabilities);
//                        if (DEBUG_PRINTING) System.out.println(desirabilities[i]);
//                    }
                    deepDesirabilities[i] = (desirabilities[i] > 1) ? 1 : 0;
                }
            }
//            String s = "";
//            while (s.length() < level) s += " ";
//            if (DEBUG_PRINTING) System.out.print(s);

            /*
             * Remind it to swipe up unless necessary
             */
            deepDesirabilities[UP] = deepDesirabilities[UP] / 2;

            if (level == 1) printList(deepDesirabilities);
            long[] out = {0, 0};
            for (int i = 0; i < 4; i++){
                if (deepDesirabilities[i] >= out[1]){
                    out[0] = i;
                    out[1] = deepDesirabilities[i];
                }
            }
            return out;
        }

        public Game getGame(){
            return game;
        }

        public double getProbability(){
            return probability;
        }

        public int getLevel(){return level;}

        /**
         * Returns a game state cloud that would result from sliding g in direction
         */
        private GameStateCloud generateCloud(Game g, int direction){
            Game copy = g.clone();
            switch (direction){
                case LEFT:
                    if (!copy.canLeft()) return new NullGameStateCloud();
                    copy.left(false);
                    break;
                case RIGHT:
                    if (!copy.canRight()) return new NullGameStateCloud();
                    copy.right(false);
                    break;
                case UP:
                    if (!copy.canUp()) return new NullGameStateCloud();
                    copy.up(false);
                    break;
                case DOWN:
                    if (!copy.canDown()){
                        //if (DEBUG_PRINTING) System.out.println("here");
                        return new NullGameStateCloud();
                    }
                    copy.down(false);
                    break;
            }
            return new GameStateCloud(new GameState(copy, 1, level));
        }
    }

    /**
     * Represents a 'cloud' of possible gamestates as the game randomly generates a tile
     */
    private class GameStateCloud{
        private ArrayList<GameState> gameStates;
        private GameState parent;
        private int level;
        private long shallowDesirability;

        public GameStateCloud(GameState gs){
            this.parent = gs;
            this.level = gs.getLevel();
            generateCloud();
            shallowDesirability = weightedAverage(gameStates);
        }

        /**
         * Never call this constructor, but don't remove it or the code will break
         */
        public GameStateCloud(){
//            if (DEBUG_PRINTING) System.out.println("here");
        }

        /**
         * generates the array of all possible gamestates after random addition of one square
         */
        private void generateCloud(){
            gameStates = new ArrayList<>();
            Game g = parent.getGame();
            int emptyCount = g.getEmptyCount();
            double prob1 = .9 / (double)emptyCount, prob2 = .1 / (double)emptyCount;
            for (int i = 0; i < emptyCount; i++){
                Game copy1 = g.clone(), copy2 = g.clone();
                copy1.getEmptyTiles().get(i).fill(2);
                copy2.getEmptyTiles().get(i).fill(4);
                gameStates.add(new GameState(copy1, prob1, level + 1));
                gameStates.add(new GameState(copy2, prob2, level + 1));
            }
        }

        /**
         * Will calculates, for each game state in the cloud, the best move and probable desirability
         * and then return the optimal output for the cloud based on the probabilities of each game state
         */
        public int getDeepDesirability(){
            int count = gameStates.size();
            //int[][] outcomes = new int[2][count];
            double total = 0;
            for (int i = 0; i < count; i++){
                //outcomes[i] = gameStates.get(i).findBestMove();
                long[] outcome = gameStates.get(i).findBestMove();
                total += outcome[1] * gameStates.get(i).getProbability();
            }
            if (total < 1 && total > 0.001) total = 1;
            return (int)total;
        }

        public long getShallowDesirability(){
            return shallowDesirability;
        }
    }

    private class NullGameStateCloud extends GameStateCloud{

        @Override
        public int getDeepDesirability(){
            return 0;
        }

        @Override
        public long getShallowDesirability(){
            return 0;
        }
    }
}
