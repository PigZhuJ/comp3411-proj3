/*********************************************
 *  Agent.java 
 *  Sample Agent for Text-Based Adventure Game
 *  COMP3411 Artificial Intelligence
 *  UNSW Session 1, 2017
 */

import java.util.*;
import java.io.*;
import java.net.*;

public class Agent {

    private HashMap<Cood, Character> map;
    private Integer currX = 0;
    private Integer currY = 0;
    private int direction = 0;
    private Queue<Character> nextMoves;
    private boolean isHugging;

    public Agent () {
        this.map = new HashMap<Cood, Character>();
        this.nextMoves = new LinkedList<Character>();
        this.isHugging = false;
    }

    public char get_action(char view[][]) {

        // default action is to go forward
        char action = 'f';

        // stitch the map given the view
        stitchMap(view);

        // if there are a list of moves to travel, then continue with the steps
        if (!nextMoves.isEmpty()) {
            action = nextMoves.poll();

        // else try to find something to do
        } else {

            // Look for items in the view
            Cood item = searchForItems(view);
            boolean canGetAnItem = false;

            // if the player can see a collectable, attempt to go to the collectable
            if (item != null) {
                canGetAnItem = aStarSearch(item);
            }

            // if you can get to the item, start poll the path set out by the queue
            if (canGetAnItem) {
                action = nextMoves.poll();

            // if there is no item or you currently can't get to an item, do standard roaming
            } else {

                // if we have started to hug the walls
                if (isHugging) {

                    // if we hit an obstacle, then turn
                    if (view[1][2] == '~' || view[1][2] == '*' || view[1][2] == 'T') {
                        action = 'r';

                    // else if we're no longer touching a wall, turn the other way
                    } else if (view[2][1] == ' ') {
                        action = 'l';
                        nextMoves.add('f');
                    }

                // else just start roaming until we hit an obstacle
                } else {

                    // if we hit an obstacle, start hugging obstacles
                    if (view[1][2] == '~' || view[1][2] == '*' || view[1][2] == 'T') {

                        action = 'r';
                        isHugging = true;

                    }

                }

            }

        }

        // Update information about the player if a player makes a certain move
        if (action == 'f') {
            updateCurrPosition();
        } else if (action == 'l') {
            direction = (direction - 1) % 4;
        } else if (action == 'r') {
            direction = (direction + 1) % 4;
        }

        print_map();
        System.out.println("*---------------------------------------*");

        return action;

    }

    private Cood searchForItems(char[][] view) {

        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                if(view[y][x] == '$' || view[y][x] == 'a' || view[y][x] == 'd' || view[y][x] == 'k') {
                    Cood itemFound = new Cood(currX + x - 2,currY + y - 2);
                    return itemFound;
                }
            }
        }
        return null;

    }

    private boolean aStarSearch(Cood destination) {

    // initialize the open list
    Queue<State> open = new PriorityQueue<State>();
    // initialize the closed list
    HashMap<Cood, Integer> closed = new HashMap<Cood, Integer>();
    // put the starting node on the open list (you can leave its f at zero)
    open.add(new State(new Cood(currX, currY),null, 0));

    // initialize the successor queue
    Queue<State> successorQueue = new LinkedList<State>();

    // while the open list is not empty
    while(!open.isEmpty()) {
        // pop the node with the least f off the open list
        State currState = open.poll();
        // generate q's 8 successors and set their parents to q
        generateSuccessors(currState, successorQueue);

        // for each successor
        while (!successorQueue.isEmpty()) {

            State successor = successorQueue.poll();
            // if successor is the goal, stop the search
            if (successor.getCurrCood().equals(destination)) {
                // TODO
                return false;
            }

            // calculate g(x)
            successor.calculateGx();
            // calculate h(x)
            successor.calculateHx(destination);

            // if a node with the same position as successor is in the OPEN list \
            // which has a lower f than successor, skip this successor


        }

    }

//

//        if a node with the same position as successor is in the CLOSED list \
//        which has a lower f than successor, skip this successor
//        otherwise, add the node to the open list
//        end
//        push q on the closed list
//        end

        return false;

    }

    public void generateSuccessors(State currState, Queue<State> successorQueue) {
        for(int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (!(x == 1 && y == 1)) {
                    State newState = new State(new Cood(currState.getCurrCood().getX() + x - 1, currState.getCurrCood().getY() + y - 1), currState, currState.getGx());
                    successorQueue.add(newState);
                }
            }
        }
    }

    public void stitchMap(char view[][]) {
        char[][] newView = rotate_view(view, direction);
        System.out.println("(" + currX + ", " + currY + ")");
        for (int x = 0; x < view.length; x++) {
            for (int y = 0; y < view.length; y++) {
                //TODO redo stitching formula
                Cood newCood = new Cood((currX + x - 2), (currY + y - 2));
                if (map.get(newCood) == null){
                    if (newView[x][y] != '\0') {
                        map.put(newCood, newView[x][y]);
                    } else {
                        map.put(newCood, 'G');
                    }
                }
            }
        }
    }

    //Rotate the view to 0 degree
    private char[][] rotate_view (char view[][], int times){
        char newView[][] = new char[view.length][view.length];
        int temp = times;
        if (temp == 0){
            return view;
        }
        while (temp % 4 != 0){
            newView = clockwise(view);
            temp++;
        }
        return newView;
    }

    //Rotate a matrix 90 degree to the right
    private char[][] clockwise (char view[][]){
        char rot_view[][] = new char[view.length][view.length];

        for (int i = 0; i < view.length; i++) {
            for (int j = 0; j < view.length; j++) {
               rot_view[i][j] = view[view.length - j - 1][i];
            }
        }
        return rot_view;
    }

    //Not really needed
    //Rotate a matrix 90 degrees to the left
    private char[][] anticlockwise (char view[][]){
        char aRot_view[][] = new char[view.length][view.length];

        for (int i = 0; i < view.length; i++) {
            for (int j = 0; j < view.length; j++) {
                aRot_view[i][j] = view[j][view.length - i - 1];
            }
        }
        return aRot_view;
    }

    private void updateCurrPosition() {
        if (direction == 0) {
            currY--;
        } else if (direction == 1) {
            currX++;
        } else if (direction == 2) {
            currY++;
        } else {
            currX--;
        }
    }

    private void print_map(){
        int sX = getSmallx();
        int sY = getSmally();
        int lX = getLargex();
        int lY = getLargey();
        System.out.println("-----------------------");
        for (int i = sX; i < lX + 1; i++) {
            System.out.print("| ");
            for (int j = sY; j < lY + 1; j++) {
                Cood accCo = new Cood(i, j);
                if (map.get(accCo) != null){
                    System.out.print(map.get(accCo) + " ");
                } else {
                    System.out.print("  ");
                }
            }
            System.out.println("|");
        }
        System.out.println("-----------------------");
    }

    private int getSmallx(){
        int x = 0;
        for(Cood coKey : map.keySet()){
            if (x > coKey.getX()){
                x = coKey.getX();
            }
        }
        return x;
    }

    private int getLargex() {
        int x = 0;
        for (Cood coKey : map.keySet()) {
            if (x < coKey.getX()) {
                x = coKey.getX();
            }
        }
        return x;
    }

    private int getSmally() {
        int y = 0;
        for (Cood coKey : map.keySet()) {
            if (y > coKey.getY()) {
                y = coKey.getY();
            }
        }
        return y;
    }

    private int getLargey() {
        int y = 0;
        for (Cood coKey : map.keySet()) {
            if (y < coKey.getY()) {
                y = coKey.getY();
            }
        }
        return y;
    }


    private void print_view(char view[][]) {
        int i, j;
        System.out.println("\n+-----------+");
        for (i = 0; i < 5; i++) {
            System.out.print("| ");
            for (j = 0; j < 5; j++) {
                if ((i == 2) && (j == 2)) {
                    System.out.print('^' + " ");
                } else {
                    System.out.print(view[i][j] + " ");
                }
            }
            System.out.println("|");
        }
        System.out.println("+-----------+");
    }

    public static void main(String[] args) {
        InputStream in = null;
        OutputStream out = null;
        Socket socket = null;
        Agent agent = new Agent();
        char view[][] = new char[5][5];
        char action = 'F';
        int port;
        int ch;
        int i, j;

        if (args.length < 2) {
            System.out.println("Usage: java Agent -p <port>\n");
            System.exit(-1);
        }

        port = Integer.parseInt(args[1]);

        try { // open socket to Game Engine
            socket = new Socket("localhost", port);
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            System.out.println("Could not bind to port: " + port);
            System.exit(-1);
        }

        try { // scan 5-by-5 window around current location
            while (true) {
                for (i = 0; i < 5; i++) {
                    for (j = 0; j < 5; j++) {
                        if (!((i == 2) && (j == 2))) {
                            ch = in.read();
                            if (ch == -1) {
                                System.exit(-1);
                            }
                            view[i][j] = (char) ch;
                        }
                    }
                }
                agent.print_view(view); // COMMENT THIS OUT BEFORE SUBMISSION
                action = agent.get_action(view);
                out.write(action);
            }
        } catch (IOException e) {
            System.out.println("Lost connection to port: " + port);
            System.exit(-1);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }
}
