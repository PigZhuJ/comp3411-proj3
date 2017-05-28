/*********************************************
 *  Agent.java
 *  Sample Agent for Text-Based Adventure Game
 *  COMP3411 Artificial Intelligence
 *  UNSW Session 1, 2017
 */

import java.util.*;
import java.io.*;
import java.net.*;

public class Agent2 {

    // Map Attributes
    private HashMap<Cood, Character> map;

    // Player Attributes
    private Queue<Character> nextMoves = new LinkedList<>();
    private ArrayList<Cood> prevPath = new ArrayList<>();
    private int direction;
    private int currX;
    private int currY;
    private boolean isHugging;
    private char hugSide;

    //Inventory
    private boolean axe;
    private boolean key;
    private boolean dynamite;
    private boolean gold;
    private boolean wood;

    private boolean onWater;

    public Agent2() {
        this.map = new HashMap<>();
        this.nextMoves = new LinkedList<>();
        this.prevPath = new ArrayList<>();
        this.direction = 0;
        this.currX = 0;
        this.currY = 0;
        this.isHugging = false;
        axe = false;
        key = false;
        dynamite = false;
        gold = false;
        wood = false;
        onWater = false;
        hugSide = ' ';
    }

    public char get_action( char view[][] ) {

        /*TODO
            - Not taking gold
            - Hugging wall loop
            - wood not changing to false after landing on land
            - s1 can be solve if not hugging trees
            - s2 not taking gold
            - s3 wall hugging loop
            - s4 A* star item loop
            - s5 A* doesn't work for water
         */
//-----------------ACTIONS BEFORE DETERMINING ACTION-----------------//

        //Creates a list of previously explored cood
        Cood prev = new Cood(currX, currY);
         if (!prevPath.isEmpty() && !prevPath.contains(prev)) {
            prevPath.add(prev);
        } else if (prevPath.isEmpty()) {
            prevPath.add(new Cood(0, 0));
        }

        stitchMap(view);
        System.out.println("Curr Pos is:" + currX + ", " + currY);
        Cood waterCheck = new Cood(currX, currY);
        System.out.println(currX + ", " + currY + " Char is: (" + map.get(waterCheck) + ")");

        if (map.get(new Cood(currX, currY)) == '~' && !onWater) {
            System.out.println("Wood is true");
            onWater = true;
            // else if you're going off water, set onWater = false and you lose the wood
        } else if (map.get(new Cood(currX, currY)) != '~' && onWater){
            onWater = false;
            wood = false;
        }
//---------------------------DETERMINING ACTION-----------------//

        // default action is to go forward
        char action = 'f';
        //debug
        listInventory();
        System.out.println(nextMoves.toString());

        // if there are a list of moves to travel, then continue with the steps
        if (!nextMoves.isEmpty()) {
            System.out.println("Already know where to go!");
            action = nextMoves.poll();
            // else try to find something to do
        } else if (nextMoves.isEmpty() && gold){
            aStarSearch(new Cood(0,0));
        } else {
            // if you can find an item
            if(scanItem(view)){
                getItem(view);
                action = nextMoves.poll();
                System.out.println("I see Items!");
            } else {
                // search the view for items that you can go to
                Cood item = searchForItems(view);
                boolean canGetAnItem = false;
                // try to get to the item
                if (item != null) {
                    canGetAnItem = aStarSearch(item);
                    System.out.println("I'm using A* search");
                }
                // if you can get to the item, then perform the preset actions to go to the item
                if (canGetAnItem) {
                    action = nextMoves.poll();
                    // if there is no item or you currently can't get to an item, do standard roaming
                } else if (scanTree(view) && axe) {
                    System.out.println("Tree Cutting");
                    cutTree(view);
                    action = nextMoves.poll();
                } else {
                    System.out.println("Exploring");
                    if (isHugging) {
                        System.out.println("Im hugging");
                        // if we hit an obstacle, then turn
                        if ((view[1][2] == '~' && !wood) || view[1][2] == '*' || view[1][2] == 'T'  || view[1][2] == '.' || view[1][2] == '-') {
                            action = rotateAtAnObstacle(view);
                            // else if we're no longer touching a wall, turn the other way
                            //TODO need to make sure wood is false when back on land
                        } else if (view[2][1] == ' ' && !wood) {
                            if (hugSide == 'l') {
                                action = 'l';
                            } else if (hugSide == 'r') {
                                action = 'r';
                            }
                            nextMoves.add('f');
                        }
                        // else just start roaming until we hit an obstacle
                    } else {
                        System.out.println("I need something to hug");
                        // if we hit an obstacle, start hugging obstacles
                        if ((view[1][2] == '~' && !wood) || view[1][2] == '*' || view[1][2] == 'T' || view[1][2] == '.' || view[1][2] == '-') {
                            action = rotateAtAnObstacle(view);
                            if (isAnObstacle(view[2][1]) || isAnObstacle(view[2][3])) {
                                isHugging = true;
                                if (action == 'l') {
                                    hugSide = action;
                                } else if (action == 'r') {
                                    hugSide = action;
                                }
                            }
                            //isHugging = true;
                        }
                    }
                }
            }
        }

//-----------------ACTIONS AFTER DETERMINING ACTION-----------------//
        //This snippet is so that AI isn't an idiot and jump into the water or go into the forest
        if (action == 'f' && (view[1][2] == '~' || view[1][2] == '.') && !wood) {
            double coinflip = Math.random() % 2;
            if (coinflip == 1) {
                action = 'l';
            } else {
                action = 'r';
            }
        }

        // update the coordinate
        if (action == 'f') {
            if (view[1][2] == '$') {
                aStarSearch(new Cood(0,0));
                gold = true;
            }
            updateCurrPosition();
        } else if (action == 'l') {
            direction = (direction + 4 - 1) % 4;
        } else if (action == 'r') {
            direction = (direction + 4 + 1) % 4;
        }

        //Water check
        if (action == 'f' && map.get(new Cood(currX, currY)) == '~') {
            onWater = true;
        }
        System.out.println("*-------------------------------------ACTION_END-------------------------------*");
        System.out.println("Action is:" + action);
        return action;

    }

    //Check if its an obstacle
    private boolean isAnObstacle(char c) {
        return (c == '*' || c == 'T' || c == '.');
    }

    //Update the absolute cood of the AI on the map
    private void updateCurrPosition() {
        if (direction == 0) {
            currY++;
        } else if (direction == 1) {
            currX++;
        } else if (direction == 2) {
            currY--;
        } else {
            currX--;
        }
    }

    //debugger
    private void listInventory(){
        System.out.println("axe: " + axe);
        System.out.println("gold: " + gold);
        System.out.println("wood: " + wood);
        System.out.println("key: " + key);
        System.out.println("dynamite: " + dynamite);
    }


//------------------AI determining code-----------------------------//

    //When met with an obstacle rotate
    private char rotateAtAnObstacle(char view[][]) {
        char action;
        if (view[2][1] == '~' || view[2][1] == '*' || view[2][1] == 'T' || view[2][1] == '.') {
            action = 'r';
            if (view[2][3] != '~' && view[2][3] != '*' && view[2][3] != 'T' && view[2][3] != '.') nextMoves.add('f');
        } else {
            action = 'l';
            nextMoves.add('f');
        }
        return action;
    }

    //put a set of move if tree is right next to AI
    private void cutTree(char[][] view) {
        int treePosX = 0;
        int treePosY = 0;
        boolean treeExist = false;

        for (int i = 1; i < view.length - 1; i++) {
            for (int j = 1; j < view.length - 1; j++) {
                if (view[i][j] == 'T') {
                    treePosX = i;
                    treePosY = j;
                    treeExist = true;
                }
            }
        }
        if (view[1][2] == 'T'){
            System.out.println("True");
            treePosX = 1;
            treePosY = 2;
        }
        if (treeExist) {
            if (treePosX == 1) {
                if (treePosY == 1) {
                    nextMoves.add('f');
                    nextMoves.add('l');
                } else if (treePosY == 2) {
                    nextMoves.add('c');
                    wood = true;
                } else if (treePosY == 3) {
                    nextMoves.add('f');
                    nextMoves.add('r');
                }
            } else if (treePosX == 2) {
                if (treePosY == 1) {
                    nextMoves.add('l');
                } else if (treePosY == 3) {
                    nextMoves.add('r');
                }
            } else if (treePosX == 3) {
                if (treePosY == 1) {
                    nextMoves.add('l');
                    nextMoves.add('f');
                    nextMoves.add('l');
                } else if (treePosY == 2) {
                    nextMoves.add('r');
                    nextMoves.add('r');
                } else if (treePosY == 3) {
                    nextMoves.add('r');
                    nextMoves.add('f');
                    nextMoves.add('r');
                }
            }
        }
    }

    //Scan the if there exist a tree one block away incl diagonal
    private boolean scanTree(char[][] view){
        boolean treeExists = false;

        for (int i = 1; i < view.length - 1; i++) {
            for (int j = 1; j < view.length - 1; j++) {
                if (view[i][j] == 'T'){
                    treeExists = true;
                }
            }
        }
        return treeExists;
    }

    //put a set of move if item is right next to AI
    private void getItem(char[][] view) {
        int itemPosX = 0;
        int itemPosY = 0;
        boolean itemExists = false;

        for (int i = 1; i < view.length-1; i++) {
            for (int j = 1; j < view.length-1; j++) {
                if (view[i][j] == 'a' || view[i][j] == '$' || view[i][j] == 'd' || view[i][j] == 'k') {
                    itemPosX = i;
                    itemPosY = j;
                    itemExists = true;
                }
            }
        }

        if (itemExists){
            if (itemPosX == 1) {
                if (itemPosY == 1) {
                    nextMoves.add('f');
                    nextMoves.add('l');
                } else if (itemPosY == 2) {
                    nextMoves.add('f');
                    if (view[1][2] == 'a') {
                        axe = true;
                    } else if (view[1][2] == '$') {
                        gold = true;
                    } else if (view[1][2] == 'd') {
                        dynamite = true;
                    } else if (view[1][2] == 'k') {
                        key = true;
                    }
                } else if (itemPosY == 3) {
                    nextMoves.add('f');
                    nextMoves.add('r');
                }
            } else if (itemPosX == 2) {
                if (itemPosY == 1) {
                    nextMoves.add('l');
                } else if (itemPosY == 3) {
                    nextMoves.add('r');
                }
            } else if (itemPosX == 3) {
                if (itemPosY == 1) {
                    nextMoves.add('l');
                    nextMoves.add('f');
                    nextMoves.add('l');
                } else if (itemPosY == 2) {
                    nextMoves.add('r');
                    nextMoves.add('r');
                } else if (itemPosY == 3) {
                    nextMoves.add('r');
                    nextMoves.add('f');
                    nextMoves.add('r');
                }
            }
        }
    }

    //Scan the if there exist an item one block away incl diagonal
    private boolean scanItem(char[][] view){
        boolean itemExists = false;

        for (int i = 1; i < view.length - 1; i++) {
            for (int j = 1; j < view.length - 1; j++) {
                if (view[i][j] == 'a' || view[i][j] == '$' || view[i][j] == 'd' || view[i][j] == 'k' ) {
                    itemExists = true;
                }
            }
        }
        return itemExists;
    }

    //Scan the view and return Cood for item
    private Cood searchForItems(char[][] view) {
        // for every y coordinate
        for (int i = 0; i < 5; i++) {
            // for every x coordinate
            for (int j = 0; j < 5; j++) {
                // if there is an item seen in the view, record the position of that
                if(view[j][i] == '$' || view[j][i] == 'a' || view[j][i] == 'd' || view[j][i] == 'k') {
                    Cood itemFound = createCood(i,j);
                    // DEBUG
                    //System.out.println("(" + itemFound.getX() + ", " + itemFound.getY() + ") => " + "(" + view[j][i] + ")");
                    return itemFound;
                }
            }
        }
        return null;
    }

//-----------------------A * Search Algorithm------------------------------------//

    private boolean aStarSearch(Cood destination) {

        // initialize the open list
        Queue<State> open = new PriorityQueue<>();
        // initialize the closed list
        ArrayList<State> closed = new ArrayList<>();
        // put the starting node on the open list (you can leave its f at zero)
        open.add(new State(new Cood(currX, currY),null, 0, 0, true));

        // while the open list is not empty
        while(!open.isEmpty()) {
            // pop the node with the least f off the open list
            System.out.println(open.toString());
            System.out.println(closed.toString());
            State currState = open.poll();
            System.out.println("Current State is at: (" + currState.getCurrCood().getX() + "," + currState.getCurrCood().getY() + ") & Fx = " + currState.calculateFx());
            // generate q's 8 successors and set their parents to q
            Queue<State> successorQueue = generateSuccessors(currState);
            // for each successor
            while (!successorQueue.isEmpty()) {
                State successor = successorQueue.poll();
                if (map.get(successor.getCurrCood()) == null) {
                    continue;
                }
                // if successor is the goal, stop the search
                if (successor.getCurrCood().equals(destination)) {
                    System.out.println("Found the path to the item at: (" + successor.getCurrCood().getX() + "," + successor.getCurrCood().getY());
                    buildNextMovesToReachItem(successor);
                    return true;
                }
                // calculate g(x)
                successor.calculateGx();
                // calculate h(x)
                successor.calculateHx(destination);
                System.out.println("Checking successor: (" + successor.getCurrCood().getX() + "," + successor.getCurrCood().getY() + ") & Gx = " + successor.getGx() + " & Hx = " + successor.getHx() + " & Fx = " + successor.calculateFx());
                boolean skipNode = false;
                // if a node with the same position as successor is in the OPEN list \
                // which has a lower f than successor, skip this successor
                for (State checkState : open) {
                    if (checkState.getCurrCood().equals(successor.getCurrCood()) &&
                            successor.calculateFx() > checkState.calculateFx()){
                        System.out.println("Denied successor in open list: (" + successor.getCurrCood().getX() + "," + successor.getCurrCood().getY() + ")");
                        skipNode = true;
                    }
                }
                // if a node with the same position as successor is in the CLOSED list \
                // which has a lower f than successor, skip this successor
                for (State checkState : closed) {
                    if (checkState.getCurrCood().equals(successor.getCurrCood()) &&
                            successor.calculateFx() > checkState.calculateFx()){
                        System.out.println("Denied successor in closed list: (" + successor.getCurrCood().getX() + "," + successor.getCurrCood().getY() + ")");
                        skipNode = true;
                    }
                }
                // if that tile cannot be traversed on, skip this successor
                if(map.get(successor.getCurrCood()) == '~' || map.get(successor.getCurrCood()) == '*' || map.get(successor.getCurrCood()) == 'T' || map.get(successor.getCurrCood()) == '.') {
                    System.out.println("Denied successor because cannot go on this tile: (" + successor.getCurrCood().getX() + "," + successor.getCurrCood().getY() + ") & Tile is " + map.get(successor.getCurrCood()));
                    skipNode = true;
                }
                // otherwise, add the node to the open list
                if(!skipNode) {
                    System.out.println("Added a successor: (" + successor.getCurrCood().getX() + "," + successor.getCurrCood().getY() + ")");
                    open.add(successor);
                }
            }
            // push q on the closed list
            closed.add(currState);
        }
        return false;
    }

    public LinkedList<State> generateSuccessors(State currState) {
        LinkedList<State> successorQueue = new LinkedList<>();
        for(int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                State newState;
                // make sure that the current player position is not recorded as a successor
                if (x == 1 && y == 0) {
                    newState = new State(new Cood(currState.getCurrCood().getX()+x-1, currState.getCurrCood().getY()+1), currState, currState.getGx(), 0, false);
                    System.out.println("Created successor: (" + newState.getCurrCood().getX() + "," + newState.getCurrCood().getY() + ") & Fx = " + newState.calculateFx());
                    System.out.println("Connected to successor: (" + currState.getCurrCood().getX() + "," + currState.getCurrCood().getY() + ") & Fx = " + currState.calculateFx());
                    System.out.println("Successor has tile " + map.get(newState.getCurrCood()));
                    successorQueue.add(newState);
                } else if (x == 0 && y == 1) {
                    newState = new State(new Cood(currState.getCurrCood().getX()+x-1, currState.getCurrCood().getY()), currState, currState.getGx(), 0, false);
                    System.out.println("Created successor: (" + newState.getCurrCood().getX() + "," + newState.getCurrCood().getY() + ") & Fx = " + newState.calculateFx());
                    System.out.println("Connected to successor: (" + currState.getCurrCood().getX() + "," + currState.getCurrCood().getY() + ") & Fx = " + currState.calculateFx());
                    System.out.println("Successor has tile " + map.get(newState.getCurrCood()));
                    successorQueue.add(newState);
                } else if (x == 2 && y == 1) {
                    newState = new State(new Cood(currState.getCurrCood().getX()+x-1, currState.getCurrCood().getY()), currState, currState.getGx(), 0, false);
                    System.out.println("Created successor: (" + newState.getCurrCood().getX() + "," + newState.getCurrCood().getY() + ") & Fx = " + newState.calculateFx());
                    System.out.println("Connected to successor: (" + currState.getCurrCood().getX() + "," + currState.getCurrCood().getY() + ") & Fx = " + currState.calculateFx());
                    System.out.println("Successor has tile " + map.get(newState.getCurrCood()));
                    successorQueue.add(newState);
                } else if (x == 1 && y == 2) {
                    newState = new State(new Cood(currState.getCurrCood().getX()+x-1, currState.getCurrCood().getY()-1), currState, currState.getGx(), 0, false);
                    System.out.println("Created successor: (" + newState.getCurrCood().getX() + "," + newState.getCurrCood().getY() + ") & Fx = " + newState.calculateFx());
                    System.out.println("Connected to successor: (" + currState.getCurrCood().getX() + "," + currState.getCurrCood().getY() + ") & Fx = " + currState.calculateFx());
                    System.out.println("Successor has tile " + map.get(newState.getCurrCood()));
                    successorQueue.add(newState);
                }

            }
        }
        return successorQueue;
    }

    private void buildNextMovesToReachItem(State successor) {
        LinkedList<Cood> moveList = new LinkedList<>();
        State currState = successor;
        System.out.println("The path to get to the item is: ");
        // retrieve all the coordinates that the player has to travel
        while(!currState.isStartingState()) {
            System.out.print("(" + currState.getCurrCood().getX() + "," +currState.getCurrCood().getY() + "), ");
            moveList.add(0, currState.getCurrCood());
            currState = currState.getPrevState();
        }
        // add the last coordinate
        //moveList.add(0, currState.getCurrCood());
        //System.out.println("(" + currState.getCurrCood().getX() + "," +currState.getCurrCood().getY() + ").");
        //System.out.println(moveList.toString());
        Cood currPosition = new Cood(currX, currY);
        int currDirection = this.direction;
        // go through the moves
        for (Cood nextPosition : moveList) {
            System.out.println("Next position is at: (" + nextPosition.getX() + "," + nextPosition.getY() + ")");
            Cood projectedPosition = calculateProjection(currPosition, currDirection);
            while(!projectedPosition.equals(nextPosition)) {
                Cood leftOfPlayer = calculateProjection(currPosition, (currDirection + 4 - 1)%4);
                if (map.get(leftOfPlayer) == '~' || map.get(leftOfPlayer) == '*' || map.get(leftOfPlayer) == 'T' || map.get(leftOfPlayer) == '.') {
                    nextMoves.add('r');
                    currDirection = (currDirection + 1)%4;
                } else {
                    nextMoves.add('l');
                    currDirection = (currDirection + 4 - 1)%4;
                }
                projectedPosition = calculateProjection(currPosition, currDirection);
                System.out.println("CurrDirection is at: " + currDirection);
                System.out.println("Testing if matched position is at: (" + projectedPosition.getX() + "," + projectedPosition.getY() + ")");
            }
            nextMoves.add('f');
            currPosition = nextPosition;
        }
    }

    private Cood calculateProjection(Cood currPosition, int currDirection) {
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
        Cood newCood = new Cood(projectedX,projectedY);
        return newCood;
    }

//-----------------------------END---------------------------------------------//

//--------------[DONE DO NOT TOUCH ANYMORE]----Map Stitching Algorithm-----------------------------//

    //Get the absolute cood of each character in the given view
    public void stitchMap(char view[][]) {
        char[][] newView = rotate_view(view, direction);
        // for each y coordinate
        for (int i = 0; i < 5; i++) {
            // for each x coordinate
            for (int j = 0; j < 5; j++) {
                // convert the viewCoordinate to the mapCoordinate
                Cood newCood = createCood(j, i);
                // record the positions of everything in the map
                if (view[j][i] != '\0') {
                    map.put(newCood, newView[i][j]);
                } else {
                    if (onWater == true) {
                        map.put(newCood, '~');
                    } else {
                        map.put(newCood, ' ');
                    }
                }
            }
        }
        // DEBUG
        print_map();
    }

    private char[][] rotate_view(char view[][], int times) {
        char newView[][] = view.clone();
        int temp = times;
        // if the view is already upright, return the view as is
        if (temp == 0) {
            return view;
            // else rotate the view until its in the right position
        } else {
            // keep rotating the view until its in the right position
            while (temp % 4 != 0) {
                newView = clockwise(newView);
                temp++;
            }
            return newView;
        }

    }

    private char[][] clockwise(char view[][]) {

        char rotatedView[][] = new char[5][5];
        // for each y coordinate
        for (int i = 0; i < 5; i++) {
            // for each x coordinate
            for (int j = 0; j < 5; j++) {
                // rotate the view
                rotatedView[i][j] = view[j][4-i];
            }
        }
        return rotatedView;

    }

    public Cood createCood(int x, int y) {

        int newX = x;
        int newY = y;

        newX = newX - 2 + currX;
        if (y == 0) {
            newY = newY + 2 + currY;
        } else if (y == 1) {
            newY = newY + 0 + currY;
        } else if (y == 2) {
            newY = newY - 2 + currY;
        } else if (y == 3) {
            newY = newY - 4 + currY;
        } else if (y == 4) {
            newY = newY - 6 + currY;
        }

        return new Cood(newX, newY);

    }

    //Print the Stitched Map out
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

    void print_view( char view[][] )
    {
        int i,j;

        System.out.println("\n+-----+");
        for( i=0; i < 5; i++ ) {
            System.out.print("|");
            for( j=0; j < 5; j++ ) {
                if(( i == 2 )&&( j == 2 )) {
                    System.out.print('^');
                }
                else {
                    System.out.print( view[i][j] );
                }
            }
            System.out.println("|");
        }
        System.out.println("+-----+");
    }

    public static void main( String[] args )
    {
        InputStream in  = null;
        OutputStream out= null;
        Socket socket   = null;
        Agent2  agent    = new Agent2();
        char   view[][] = new char[5][5];
        char   action   = 'F';
        int port;
        int ch;
        int i,j;

        if( args.length < 2 ) {
            System.out.println("Usage: java Agent -p <port>\n");
            System.exit(-1);
        }

        port = Integer.parseInt( args[1] );

        try { // open socket to Game Engine
            socket = new Socket( "localhost", port );
            in  = socket.getInputStream();
            out = socket.getOutputStream();
        }
        catch( IOException e ) {
            System.out.println("Could not bind to port: "+port);
            System.exit(-1);
        }

        try { // scan 5-by-5 wintow around current location
            while( true ) {
                for( i=0; i < 5; i++ ) {
                    for( j=0; j < 5; j++ ) {
                        if( !(( i == 2 )&&( j == 2 ))) {
                            ch = in.read();
                            if( ch == -1 ) {
                                System.exit(-1);
                            }
                            view[i][j] = (char) ch;
                        }
                    }
                }
                agent.print_view( view ); // COMMENT THIS OUT BEFORE SUBMISSION
                action = agent.get_action( view );
                out.write( action );
            }
        }
        catch( IOException e ) {
            System.out.println("Lost connection to port: "+ port );
            System.exit(-1);
        }
        finally {
            try {
                socket.close();
            }
            catch( IOException e ) {}
        }
    }
}
