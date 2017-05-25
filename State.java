import java.util.Queue;

/**
 * Created by Andrew on 25/05/2017.
 */
public class State {

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

}
