import java.util.Queue;

/**
 * Created by Andrew on 25/05/2017.
 */
public class State implements Comparable<State> {

    private Cood currCood;
    private State prevState;
    private int gx;
    private int hx;

    public State(Cood currCood, State prevState, int gx) {
        this.currCood = currCood;
        this.prevState = prevState;
        this.gx = gx;
    }

    public void calculateGx() {
        gx++;
    }

    public void calculateHx(Cood destination) {
        this.hx = Math.abs(currCood.getX() - destination.getX()) + Math.abs(currCood.getY() - destination.getY());
    }

    public Cood getCurrCood() {
        return this.currCood;
    }

    public int getGx() {
        return this.gx;
    }

    public int getHx() { return this.hx; }

    public int calculateFx() { return this.gx + this.hx; }

    public State getPrevState () {
        return this.prevState;
    }

    @Override
    public int compareTo(State compareState) {
        int compareGCost = compareState.getGx();
        int compareHCost = compareState.getHx();
        int compareCost = compareGCost + compareHCost;

        int cost = this.gx + this.hx;
        return cost - compareCost;
    }
}
