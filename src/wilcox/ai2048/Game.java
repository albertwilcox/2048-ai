package wilcox.ai2048;

import java.util.ArrayList;
import java.util.Random;

public class Game {

    private static final int SIZE = 4;

    private Tile[][] board;
    private Random r;
    private int score = 0;

    public Game(){
        board = new Tile[SIZE][SIZE];
        r = new Random();

        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                board[i][j] = new Tile(this);

        spawnTile();
        spawnTile();
//        spawnTile(1, 0);
//        spawnTile(3, 3);
    }

    private Game(Tile[][] board, int score){
        this.board = board;
        this.score = score;
        r = new Random();

        for (Tile[] row : board)
            for (Tile tile: row)
                tile.setGame(this);
    }

    public ArrayList<Tile> getEmptyTiles(){
        ArrayList<Tile> out = new ArrayList<>();
        for (Tile[] row : board)
            for (Tile tile: row)
                if (tile.isEmpty())
                    out.add(tile);
        return out;
    }

    public int getEmptyCount(){
        return getEmptyTiles().size();
    }

    public void spawnTile(){
        ArrayList<Tile> empties = getEmptyTiles();
        empties.get(r.nextInt(empties.size())).fill(r);
    }

    /**
     * Spawns a tile for debug purposes
     * @param x
     * @param y
     */
    public void spawnTile(int x, int y, int value){
        board[y][x].fill(value);
    }

    /**
     * Attempts to place a tile on the board at (x, y) with value value, returns whether it was successful
     *
     * Only use this for AI looking ahead
     *
     * @param x
     * @param y
     * @param value
     * @return
     */
    public boolean placeTile(int x, int y, int value){
        if (!board[y][x].isEmpty()) return false;
        board[y][x].fill(value);
        return true;
    }

    public boolean debugSwap(int x1, int x2, int y1, int y2){
        return board[y1][x1].swap(board[y2][x2]);
    }

    public void increaseScore(int change){
        score += change;
    }

    public int maxTile(){
        int max = 2;
        for (Tile[] row : board)
            for (Tile tile: row)
                if (tile.getValue() > max) max = tile.getValue();
        return max;
    }

    /**
     * Slide things left
     */
    public void left(){
        left(true);
    }

    /**
     * Slide things right
     */
    public void right(){
        right(true);
    }

    /**
     * Slide things up
     */
    public void up(){
        up(true);
    }

    /**
     * Slide things down
     */
    public void down(){
        down(true);
    }

    /**
     * Slide things left
     */
    public void left(boolean spawn){
        if (!canLeft()) return;
        for (int row = 0; row < 4; row++){
            for (int col = 1; col < 4; col++){
                // The following loop basically shifts the square as far as it will go
                int offset = 0;
                while(offset+col-1 >= 0 && board[row][col+offset].swap(board[row][col-1+offset])){
                    offset--;
                }
            }
        }
        if (spawn) spawnTile();
        resetMergables();
    }

    /**
     * Slide things right
     */
    public void right(boolean spawn){
        if (!canRight()) return;
        for (int row = 0; row < 4; row++){
            for (int col = 3; col >= 0; col--){
                int offset = 0;
                while(offset+col+1 < 4 && board[row][col+offset].swap(board[row][col+1+offset])){
                    offset++;
                }
            }
        }
        if (spawn) spawnTile();
        resetMergables();
    }

    /**
     * Slide things up
     */
    public void up(boolean spawn){
        if (!canUp()) return;
        for (int row = 1; row < 4; row++){
            for (int col = 0; col < 4; col++){
                int offset = 0;
                while(offset+row-1 >= 0 && board[row+offset][col].swap(board[row+offset-1][col])){
                    offset--;
                }
            }
        }
        if (spawn) spawnTile();
        resetMergables();
    }

    /**
     * Slide things down
     */
    public void down(boolean spawn){
        if (!canDown()) return;
        for (int row = 2; row >= 0; row--){
            for (int col = 0; col < 4; col++){
                int offset = 0;
                while(offset+row+1 < 4 && board[row+offset][col].swap(board[row+offset+1][col])){
                    offset++;
                }
            }
        }
        if (spawn) spawnTile();
        resetMergables();
    }

    public boolean canLeft(){
        boolean can = false;
        for (int row = 0; row < 4; row++){
            for (int col = 1; col < 4; col++){
                if (!board[row][col].isEmpty() && (board[row][col-1].isEmpty() || board[row][col-1].canMerge(board[row][col])))
                    can = true;
            }
        }
        return can;
    }

    public boolean canRight(){
        boolean can = false;
        for (int row = 0; row < 4; row++){
            for (int col = 2; col >= 0; col--){
                if (!board[row][col].isEmpty() && (board[row][col+1].isEmpty() || board[row][col+1].canMerge(board[row][col])))
                    can = true;
            }
        }
        return can;
    }

    public boolean canUp(){
        boolean can = false;
        for (int row = 1; row < 4; row++){
            for (int col = 0; col < 4; col++){
                if (!board[row][col].isEmpty() && (board[row-1][col].isEmpty() || board[row-1][col].canMerge(board[row][col])))
                    can = true;
            }
        }
        return can;
    }

    public boolean canDown(){
        boolean can = false;
        for (int row = 2; row >= 0; row--){
            for (int col = 0; col < 4; col++){
                if (!board[row][col].isEmpty() && (board[row+1][col].isEmpty() || board[row+1][col].canMerge(board[row][col])))
                    can = true;
            }
        }
        return can;
    }

    public boolean isGridLocked(){
        return !(canLeft() || canRight() || canDown() || canUp());

    }

    public void resetMergables(){
        for (Tile[] row : board)
            for (Tile tile: row)
                tile.makeMergable();
    }

    public Tile[][] getBoard(){
        return board;
    }

    public int getScore(){
        return score;
    }

    public String testString(){
        String s = "";
        for (Tile[] row : board){
            for (Tile tile: row){
                s = s + tile.testString() + " ";
            }
            s += "\n";
        }
        return s;
    }

    @Override
    public Game clone(){
        Tile[][] board = new Tile[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                board[i][j] = this.board[i][j].clone();
        return new Game(board, score);
    }
}
