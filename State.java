
public class State {

    private boolean hasGold;
    private char[][] view;

    public State(boolean hasGold, char[][] view) {
        this.hasGold = hasGold;
        this.view = view;
    }

    public boolean getHasGold() {
        return hasGold;
    }

    public char[][] getView() {
        return view;
    }

    public void  setHasGold(boolean hasGold) {
        this.hasGold = hasGold;
    }

    public void setView(char[][] view){
        this.view = view;
    }


}
