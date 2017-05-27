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
    private int moves = 0;
    private ArrayList<Character> prevMove = new ArrayList<>();

    //Inventory
    private boolean axe = false;
    private boolean key = false;
    private boolean dynamite = false;
    private boolean gold = false;
    private boolean wood = false;

    public Agent () {
        this.map = new HashMap<Cood, Character>();
        this.nextMoves = new LinkedList<Character>();
        this.isHugging = false;
    }

    public char get_action(char view[][]) {

        // default action is to go forward
        char action = 'f';

        //For debugging purposes
//        System.out.println("Current Pos: " + currX + ", " + currY);
//        System.out.println("direction is: " + direction);

        // if there are a list of moves to travel, then continue with the steps
        if (!nextMoves.isEmpty()) {
            action = nextMoves.poll();

            // else try to find something to do
        } else {

            // Look for items in the view
            Cood item = searchForItems(view);
            boolean canGetAnItem = false;

            // if the player can see a collectable, attempt to go to the collectable
            //TODO
//            if (item != null) {
//                canGetAnItem = aStarSearch(item);
//            }

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


        // stitch the map given the view
        if (action == 'f') {
            stitchMap(view);
        }
        // Update information about the player if a player makes a certain move
        if (action == 'f') {
            updateCurrPosition();
        } else if (action == 'l') {
            direction = (direction - 1) % 4;
        } else if (action == 'r') {
            direction = (direction + 1) % 4;
        }

        //For debugging purposes
        System.out.println("*-------------------END--------------------*");
        moves++;
        if (moves == 50){
            System.exit(0);
        }
        //add curr move into arraylist of prev move
        prevMove.add(action);
        return action;

    }

    //Scan the view and return Cood for item
    private Cood searchForItems(char[][] view) {
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                // if there is an item seen in the view, record the position of that
                if(view[x][y] == '$' || view[x][y] == 'a' || view[x][y] == 'd' || view[x][y] == 'k') {
                    Cood itemFound = createCood(x,y);
                    System.out.println("(" + itemFound.getX() + ", " + itemFound.getY() + ") => " + "(" + view[x][y] + ")");
                    return itemFound;
                }
            }
        }
        return null;
    }

    private boolean aStarSearch(Cood destination) {

        // initialize the open list
        Queue<State> open = new PriorityQueue<>();
        // initialize the closed list
        ArrayList<State> closed = new ArrayList<>();
        // put the starting node on the open list (you can leave its f at zero)
        open.add(new State(new Cood(currX, currY),null, 0));

        Queue<State> successorQueue = new LinkedList<>();

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
                    buildNextMovesToReachItem(successor);
                    return true;
                }

                // calculate g(x)
                successor.calculateGx();
                // calculate h(x)
                successor.calculateHx(destination);

                boolean skipNode = false;

                // if a node with the same position as successor is in the OPEN list \
                // which has a lower f than successor, skip this successor
                for (State checkState : open) {
                    if (checkState.getCurrCood().equals(successor.getCurrCood()) &&
                            successor.calculateFx() > checkState.calculateFx()){
                        skipNode = true;
                    }
                }

                // if a node with the same position as successor is in the CLOSED list \
                // which has a lower f than successor, skip this successor
                for (State checkState : closed) {
                    if (checkState.getCurrCood().equals(successor.getCurrCood()) &&
                            successor.calculateFx() > checkState.calculateFx()){
                        skipNode = true;
                    }
                }

                // otherwise, add the node to the open list
                if(!skipNode) {
                    open.add(successor);
                }

            }

            // push q on the closed list
            closed.add(currState);

        }

        return false;

    }

    private void buildNextMovesToReachItem(State successor) {
        LinkedList<Cood> moveList = new LinkedList<>();
        State currState = successor;
        // retrieve all the coordinates that the player has to travel
        while(!currState.getPrevState().equals(null)) {
            moveList.add(0, currState.getCurrCood());
            currState = currState.getPrevState();
        }
        // add the last coordinate
        //moveList.add(0, currState.getCurrCood());
        Cood currPosition = new Cood(currX, currY);
        int currDirection = this.direction;
        // go through the moves
        for (Cood nextPosition : moveList) {
            Cood projectedPosition = calculatePosition(currPosition, currDirection);
            currPosition = nextPosition;
        }
    }

    private Cood calculatePosition(Cood currPosition, int currDirection) {
        int projectedX = currPosition.getX();
        int projectedY = currPosition.getY();
        if (currDirection == 0) {
            projectedY++;
        } else if (currDirection == 1) {
            projectedX++;
        } else if (currDirection == 2) {
            projectedY--;
        } else {
            projectedX--;
        }
//        Cood newCood = new Cood();
        return null;
    }

    public void generateSuccessors(State currState, Queue<State> successorQueue) {
        for(int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                // make sure that the current player position is not recorded as a successor
                if (!(x == 1 && y == 1)) {
                    State newState = new State(new Cood(currState.getCurrCood().getX() + x - 1, currState.getCurrCood().getY() + y - 1), currState, currState.getGx());
                    successorQueue.add(newState);
                }
            }
        }
    }

    //Update Current position based on direction
    private void updateCurrPosition() {
        if (direction == 0) {
            currY++;
        } else if (direction == 1) {
            currX--;
        } else if (direction == 2) {
            currY--;
        } else {
            currX++;
        }
    }

//--------------------------------------Map Stitching Algorithm-----------------------------//
    //Get the absolute cood of each character in the given view
    public void stitchMap ( char view[][]){
        char[][] newView = rotate_view(view, direction);
        //For debugging purposes
//        print_view(newView);
        for (int i = 0; i < newView.length; i++) {
            for (int j = 0; j < newView.length; j++) {
                Cood newCood = createCood(i, j);
                //For debugging purposes
//                System.out.println("(" + i + ", " + j + ") Symbol is: (" + newView[i][j] + ")");
//                if (map.get(newCood) == null) {
                    if (view[j][i] != '\0') {
                        map.put(newCood, newView[i][j]);
                    } else {
                        map.put(newCood, ' ');
//                    }
                }
                //For debugging purposes
//                System.out.println("(" + i + ", " + j + ") -> (" + newCood.getX() + ", " + newCood.getY() + ") => (" + newView[i][j] + ") => (" + map.get(newCood) + ")");
            }
        }
        //For debugging purposes
        print_map();
    }

    //Rotate the view to 0 degree
    private char[][] rotate_view (char view[][], int times){
        char newView[][] = view;
        int temp = times;
        if (temp == 0 && currX == 0){
            return view;
        } else {
            newView = flip_view(newView);
            //For debugging purposes
//            System.out.println("Flipped");
//            print_view(newView);
            while (temp % 4 != 0){
                newView = clockwise(newView);

                temp++;
                //For debugging purposes
//                System.out.println("Rotation is: " + temp);
            }
            return newView;
        }
    }

    //flip the view for rotate_view
    public static char[][] flip_view(char[][] view) {
        char[][] newView = new char[view.length][view.length];
        for (int i = 0; i < view.length; i++) {
            for (int j = 0; j < view.length; j++) {
                newView[i][view.length - j - 1] = view[i][j];
            }
        }
        return newView;
    }

    //Rotate a matrix 90 degree to the right for rotate_view
    private char[][] clockwise (char view[][]){
        char rot_view[][] = new char[view.length][view.length];

        for (int i = 0; i < view.length; i++) {
            for (int j = 0; j < view.length; j++) {
               rot_view[i][j] = view[view.length - j - 1][i];
            }
        }
        return rot_view;
    }

    //Custom Cood works
    public Cood createCood(int x, int y) {
        int newX = x;
        int newY = y;
        if (x == 0) {
            if (y == 0) {
                newX = newX - 2 + currX;
                newY = newY + 2 + currY;
            } else if (y == 1) {
                newX = newX - 1 + currX;
                newY = newY + 1 + currY;
            } else if (y == 2) {
                newX = newX + 0 + currX;
                newY = newY + 0 + currY;
            } else if (y == 3) {
                newX = newX + 1 + currX;
                newY = newY - 1 + currY;
            } else if (y == 4) {
                newX = newX + 2 + currX;
                newY = newY - 2 + currY;
            }
        } else if (x == 1) {
            if (y == 0) {
                newX = newX - 3 + currX;
                newY = newY + 1 + currY;
            } else if (y == 1) {
                newX = newX - 2 + currX;
                newY = newY + 0 + currY;
            } else if (y == 2) {
                newX = newX - 1 + currX;
                newY = newY - 1 + currY;
            } else if (y == 3) {
                newX = newX + 0 + currX;
                newY = newY - 2 + currY;
            } else if (y == 4) {
                newX = newX + 1 + currX;
                newY = newY - 3 + currY;
            }
        } else if (x == 2) {
            if (y == 0) {
                newX = newX - 4 + currX;
                newY = newY + 0 + currY;
            } else if (y == 1) {
                newX = newX - 3 + currX;
                newY = newY - 1 + currY;
            } else if (y == 2) {
                newX = newX - 2 + currX;
                newY = newY - 2 + currY;
            } else if (y == 3) {
                newX = newX - 1 + currX;
                newY = newY - 3 + currY;
            } else if (y == 4) {
                newX = newX + 0 + currX;
                newY = newY - 4 + currY;
            }
        } else if (x == 3) {
            if (y == 0) {
                newX = newX - 5 + currX;
                newY = newY - 1 + currY;
            } else if (y == 1) {
                newX = newX - 4 + currX;
                newY = newY - 2 + currY;
            } else if (y == 2) {
                newX = newX - 3 + currX;
                newY = newY - 3 + currY;
            } else if (y == 3) {
                newX = newX - 2 + currX;
                newY = newY - 4 + currY;
            } else if (y == 4) {
                newX = newX - 1 + currX;
                newY = newY - 5 + currY;
            }
        } else if (x == 4) {
            if (y == 0) {
                newX = newX - 6 + currX;
                newY = newY - 2 + currY;
            } else if (y == 1) {
                newX = newX - 5 + currX;
                newY = newY - 3 + currY;
            } else if (y == 2) {
                newX = newX - 4 + currX;
                newY = newY - 4 + currY;
            } else if (y == 3) {
                newX = newX - 3 + currX;
                newY = newY - 5 + currY;
            } else if (y == 4) {
                newX = newX - 2 + currX;
                newY = newY - 6 + currY;
            }
        }
        return new Cood(newX, newY);
    }

    //Print out the map for debugging purposes
    private void print_map(){
        int xs = getSmallx();
        int ys = getSmally();
        int xl = getLargex();
        int yl = getLargey();

        int smallest = 0;
        int largest = 0;

        //Find the smallest point
        if (xs <= ys){
            smallest = xs;
        } else if (xs >= ys){
            smallest = ys;
        }

        //Find the largest point
        if (xl >= yl){
            largest = xl;
        } else if (xl <= yl){
            largest = yl;
        }
        //For debugging purposes
//        System.out.println("--------------------------------");
//        System.out.println("Small: (" + xs + ", " + ys + ")");
//        System.out.println("Large: (" + xl + ", " + yl + ")");
//        System.out.println("--------------------------------");
//        System.out.println("From the hashmap:");
//        for(Cood key : map.keySet()){
//            System.out.println("(" + key.getX() + ", " + key.getY() + ") => (" + map.get(key) + ")");
//        }
        //For debugging purposes
//        System.out.println("----------------");
//        for (int i = smallest; i < largest + 1; i++) {
//            for (int j = smallest; j < largest + 1; j++) {
//                if (i >= 0 && j >= 0){
//                    System.out.print(" (" + j + ", " + i + ")");
//                } else {
//                    System.out.print(" (" + j + ", " + i + ")");
//                }
//            }
//            System.out.println();
//        }
//        System.out.println("----------------");

        System.out.println("----------------------");
        for (int i = smallest; i < largest + 1; i++) {
            System.out.print("| ");
            for (int j = smallest; j < largest + 1; j++) {
                Cood accCo = new Cood(j, i);
                if (map.get(accCo) != null){
                    System.out.print(map.get(accCo) + " ");
                } else {
                    System.out.print("x ");
                }
            }
            System.out.println("|");
        }
        System.out.println("----------------------");
    }

    //For print_map
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
//--------------------------------------END--------------------------------------------------//

    //Change back to original function after finish
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
