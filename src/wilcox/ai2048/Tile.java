package wilcox.ai2048;

import java.util.Random;

public class Tile {
    private boolean empty;
    private int value;
    private boolean unmergable = false;

    private Game game;

    public Tile(boolean empty, int value, Game game){
        this.empty = empty;
        this.value = value;
        this.game = game;
    }

    public Tile(boolean empty, Game game){
        this(empty, 0, game);
        if (!empty){
            Random r = new Random();
            if (r.nextFloat() < .9)
                value = 2;
            else
                value = 4;
        }
    }

    public Tile(Game game){
        this(true, game);
    }

    public boolean isEmpty(){
        return empty;
    }

    /**
     * Call this method within the tile that's moving, and use the destination tile as the parameter
     *
     * whoever named this tile was an idiot
     *
     * If other is empty it swaps them and returns true
     * If other is not empty it merges them if possible, then returns false
     * If this is empty return false
     * @param other the tile to attempt to swap with
     * @return whether the movement algorithms should keep swapping things
     */
    public boolean swap(Tile other){
        if (this.isEmpty()) {
//            System.out.println("This is empty");
            return false;
        }
        if (other.isEmpty()){
//            System.out.println("Other is empty");
            other.value = value;
            other.empty = false;
            value = 0;
            empty = true;
            return true;
        } else {
            if (other.canMerge(this))
                other.merge(this);
            return false;
        }
    }

    /**
     * returns whether this tile can accept other
     * @param other
     * @return
     */
    public boolean canMerge(Tile other){
        return other.value == value && !unmergable;
    }

    /**
     * Accepts other for merging, eg this tile will be the destination
     * @param other
     */
    public void merge(Tile other){
        other.value = 0;
        other.empty = true;
        value *= 2;
        unmergable = true;
        game.increaseScore(value);
    }

    public void makeMergable(){
        unmergable = false;
    }

    public void fill(Random r){
        empty = false;
        if (r.nextFloat() < .9)
            value = 2;
        else
            value = 4;
    }

    public void fill(int value){
        empty = false;
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public void setGame(Game game){
        this.game = game;
    }

    public String testString(){
        String s = Integer.toString(value);
        while (s.length() < 4){
            s = " " + s;
        }
        return s;
    }

    @Override
    public Tile clone(){
        return new Tile(empty, value, game);
    }
}
