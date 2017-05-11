
public class State {

    private boolean hasGold;
    private char[][] viewAtState;

    public State(boolean hadKey, char[][] viewAtState) {
        this.hasGold = false;
        this.viewAtState = viewAtState.clone();
    }

    public boolean getHasGold() {
        return hasGold;
    }

    public char[][] getViewAtState(){
        return viewAtState;
    }

    public void setHasGold(boolean hasKey) {
        this.hasGold = hasGold;
    }

}
