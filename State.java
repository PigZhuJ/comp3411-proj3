
public class State {

    private boolean hasGold;
    private char[][] viewAtState;

    public State(boolean hasGold, char[][] viewAtState) {
        this.hasGold = hasGold;
        this.viewAtState = viewAtState.clone();
    }

    public boolean getHasGold() {
        return hasGold;
    }

    public char[][] getViewAtState(){
        return viewAtState;
    }
}
