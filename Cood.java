/**
 * Created by Saus on 25/5/17.
 */
public class Cood {
    private int x;
    private int y;

    public Cood (int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Cood)) {
            return false;
        }
        Cood cood = (Cood) o;
        return cood.getX() == x && cood.getY() == y;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + x;
        result = 31 * result + y;
        return result;
    }

}
