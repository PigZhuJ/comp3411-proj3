
public class State {

    private boolean hasKey;
    private char[][] viewAtState;
    
    public State(boolean hadKey, char[][] viewAtState) {
        this.hasKey = false;
        this.viewAtState = viewAtState.clone();
    }
    
    public boolean getHasKey() {
        return hasKey;
    }
    
    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }
    
}
