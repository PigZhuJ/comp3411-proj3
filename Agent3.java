/*********************************************
 *  Agent.java
 *  Sample Agent for Text-Based Adventure Game
 *  COMP3411 Artificial Intelligence
 *  UNSW Session 1, 2017
 */

import java.util.*;
import java.io.*;
import java.net.*;

public class Agent3 {

    // Map Related Fields
    private HashMap<Cood, Character> map;
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;

    // Player Related Fields
    private int currX;
    private int currY;
    private int direction;
    private Queue<Character> nextMoves = new LinkedList<>();
    private boolean onWater;
    private boolean isHuggingWall;
    private char hugSide;

    // Player Inventory Fields
    private boolean wood;
    private boolean gold;
    private boolean axe;
    private boolean key;
    private int dynamite;

    // DEBUG
    private int moves;

    public Agent3() {
        // Map Related Fields
        this.map = new HashMap<>();
        this.minX = 0;
        this.minY = 0;
        this.maxX = 0;
        this.maxY = 0;
        // Player Related Fields
        this.currX = 0;
        this.currY = 0;
        this.direction = 0;
        this.nextMoves = new LinkedList<>();
        this.onWater = false;
        this.isHuggingWall = false;
        this.hugSide = ' ';
        // Player Inventory Fields
        this.wood = false;
        this.gold = false;
        this.axe = false;
        this.key = false;
        this.dynamite = 0;
        // DEBUG
        this.moves = 0;
    }

    public char get_action( char view[][] ) {
        //-----------------STEPS BEFORE DETERMINING PLAYER ACTION-----------------//
        listInventory();
        stitchMap(view);
        if (gold && nextMoves.isEmpty()) {
            System.out.println("Going home now!");
            aStarSearch(new Cood(0, 0));
        }
        // DEBUGG STATEMENTS
        System.out.println("Curr Player Position is: (" + currX + "," + currY + ")");
        System.out.println(nextMoves.toString());

//-----------------DETERMINING PLAYER ACTION------------------------------//
        char action = 'f';
        // if there are a list of moves to travel, then continue with the steps
        if (!nextMoves.isEmpty()) {
            System.out.println("Already know where to go!");
            action = nextMoves.poll();
            // else try to find something to do
        } else {
            boolean canGetItem = searchForItems(view);
            boolean canCutTree = false;
            // search for an item to get to in the view
            Cood nearbyTree = scanTree(view);

            if (nearbyTree != null && axe && !canGetItem) {
                System.out.println("Tree Cutting");
                if (aStarSearch(nearbyTree)) {
                    canCutTree = true;
                }
            }

            if (canGetItem || canCutTree) {
                action = nextMoves.poll();
            } else {
                System.out.println("Exploring");
                // if the player is hugging the walls
                if (isHuggingWall) {
                    System.out.println("I'm hugging");
                    // if we hit an obstacle, then turn
                    /*if (isAnObstacle(view[1][2])) {
                        action = rotateAtAnObstacle(view);
                    // else if we don't have a wall to hug i.e.
                    //   ^   *
                    // *     *
                    // * * * *
                    // If the player is hugging the left
                    } else */if (hugSide == 'l') {
                        // if the left of player is empty and is on land, rotate left
                        // always look left first to continue hugging walls
                        // go forward if you can
                        if (isAnObstacle(view[3][1], dynamite, false) && view[2][1] == ' ') {
                            action = 'l';
                            nextMoves.add('f');
                        } else if (isAnObstacle(view[2][1], dynamite, false) && isAnObstacle(view[1][2], dynamite, false)) {
                            action = 'r';
                        } else if (isAnObstacle(view[2][3], dynamite, false) && isAnObstacle(view[1][2], dynamite, false)) {
                            action = 'l';
                            nextMoves.add('f');
                        } else if (view[1][2] == ' ' && isAnObstacle(view[2][1], dynamite, false)) {
                            action = 'f';
                        }
                        /*if (view[1][2] == ' ' && isAnObstacle(view[2][1])) {
                            action = 'f';
                        } else if (isAnObstacle(view[1][1]) && isAnObstacle(view[3][2]) && !onWater) {
                            action = 'f';
                        } else if (view[2][1] == ' ' && !onWater) {
                            action = 'l';
                            nextMoves.add('f');
                        } else if (view[2][3] == ' ' && !onWater) {
                            action = 'r';
                            nextMoves.add('f');
                        }
                        //nextMoves.add('f');*/
                        // *   ^
                        // *     *
                        // * * * *
                        // If the player is hugging the right
                    } else if (hugSide == 'r') {
                        // if the right of player is empty and is on land, rotate right
                        // always look right first to continue hugging walls
                        if (isAnObstacle(view[3][3], dynamite, true) && view[2][3] == ' ') {
                            action = 'r';
                            nextMoves.add('f');
                        } else if (isAnObstacle(view[2][3], dynamite, false) && isAnObstacle(view[1][2], dynamite, false)) {
                            action = 'l';
                        } else if (isAnObstacle(view[2][1], dynamite, false) && isAnObstacle(view[1][2], dynamite, false)) {
                            action = 'r';
                            nextMoves.add('f');
                        } else if (view[1][2] == ' ' && isAnObstacle(view[2][3], dynamite, false)) {
                            action = 'f';
                        }
                        /*if (view[1][2] == ' ' && isAnObstacle(view[2][3])) {
                            action = 'f';
                        } else if (isAnObstacle(view[1][3]) && isAnObstacle(view[3][2]) && !onWater) {
                            action = 'f';
                        } else if (view[2][3] == ' ' && !onWater) {
                            action = 'r';
                            nextMoves.add('f');
                        } else if (view[2][1] == ' ' && !onWater) {
                            action = 'l';
                            nextMoves.add('f');
                        }
                        //nextMoves.add('f');*/
                    }
                    // else do standard roaming
                } else {
                    System.out.println("I need something to hug");
                    // if we hit an obstacle
                    if (isAnObstacle(view[1][2], dynamite, false)) {
                        // rotate to avoid obstacles
                        action = rotateAtAnObstacle(view);
                        // if we are at a corner, start hugging that section of the block
                        if (isAnObstacle(view[2][1], dynamite, false) || isAnObstacle(view[2][3], dynamite, false)) {
                            isHuggingWall = true;
                            // determining which side of the player is going to hug
                            if (action == 'l') {
                                hugSide = 'r';
                            } else if (action == 'r') {
                                hugSide = 'l';
                            }
                        }
                    }
                }
                // if we hit an obstacle
                /*if (isHuggingWall) {
                    // if we hit an obstacle, then turn
                    if ((view[1][2] == '~') || view[1][2] == '*' || view[1][2] == 'T' || view[1][2] == '.' || view[1][2] == '-') {
                        action = rotateAtAnObstacle(view);
                        // else if we're no longer touching a wall, turn the other way
                    } else if (view[2][1] == ' ') {
                        action = 'l';
                        nextMoves.add('f');
                        // else just start roaming until we hit an obstacle
                    }
                } else {
                    // if we hit an obstacle, start hugging obstacles
                    if ((view[1][2] == '~') || view[1][2] == '*' || view[1][2] == 'T' || view[1][2] == '.' || view[1][2] == '-') {
                        action = rotateAtAnObstacle(view);
                        isHuggingWall = true;
                    }
                }*/
            }
        }

        if (action == 'f' && (view[1][2] == '.' || (view[1][2] == '~' && !wood) || view[1][2] == '*')) {
            System.out.println("Oh shit!");
            double coinFlip = Math.random() % 2;
            if (coinFlip == 0) {
                action = 'l';
            } else {
                action = 'r';
            }
        }
//-----------------STEPS AFTER DETERMINING PLAYER ACTION------------------//
        if (action == 'f') {
            if (view[1][2] == '$') {
                gold = true;
                for (Cood i : map.keySet()) {
                    if (map.get(i) == '$') {
                        map.put(i, ' ');
                    }
                }
            } else if (view[1][2] == 'a') {
                axe = true;
            } else if (view[1][2] == 'd') {
                dynamite++;
                //System.out.print(dynamite);
                //System.exit(0);
            } else if (view[1][2] == '~'){
                onWater = true;
            } else if (view[1][2] == ' ' && onWater){
                onWater = false;
                wood = false;
            }
            if (view[1][2] != '*' || view[1][2] != 'T' || view[1][2] != '-') {
                updateCurrPosition();
            } else {
                System.out.println("Ouch!");
            }
            // no long hug walls if the player is entering new territory
            if (currX < minX) {
                minX = currX;
                isHuggingWall = false;
            } else if (currX > maxX) {
                maxX = currX;
                isHuggingWall = false;
            } else if (currY < minY) {
                minY = currY;
                isHuggingWall = false;
            } else if (currY > maxY) {
                maxY = currY;
                isHuggingWall = false;
            }
            // update the direction if turning
        } else if (action == 'l') {
            direction = (direction + 4 - 1) % 4;
        } else if (action == 'r') {
            direction = (direction + 4 + 1) % 4;
        } else if (action == 'b' && dynamite != 0) {
            dynamite--;
        } else if (action == 'c'){
            wood = true;
        } else if (action == 'k')

        // DEBUG
        if (moves < 2000) {
            moves++;
        } else {
            System.exit(0);
        }

        System.out.print("List of Next move are: ");
        System.out.println(nextMoves.toString());
        System.out.println("The next move is: " + action);
        System.out.println("*----------------------------END-------------------------*");
        return action;
    }

    private void listInventory() {
        System.out.println("axe: " + axe);
        System.out.println("gold: " + gold);
        System.out.println("wood: " + wood);
        System.out.println("key: " + key);
        System.out.println("dynamite: " + dynamite);
        System.out.println("On water: " + onWater);
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

    //Check if its an obstacle
    private boolean isAnObstacle(char c, int dynamites, boolean isSearch) {
        if (isSearch) {
            return ((c == '~' && !wood) || (c == '*' && dynamites == 0) || (c == 'T' && !axe) || c == '.' || (c == '-' && !key));
        } else {
            return ((c == '~' && !wood) || (c == '*') || (c == 'T' && !axe) || c == '.' || (c == '-' && !key));
        }
        //System.out.println(c + " " + dynamites + " " + (c == '*' && dynamites > 0));

    }

    //When met with an obstacle rotate
    private char rotateAtAnObstacle(char view[][]) {
        char action;
        if (isAnObstacle(view[2][1], dynamite, false)) {
            action = 'r';
            //if (view[2][3] != '~' && view[2][3] != '*' && view[2][3] != 'T' && view[2][3] != '.') nextMoves.add('f');
        } else {
            action = 'l';
            //nextMoves.add('f');
        }
        return action;
    }

    //Scan the if there exist a tree one block away incl diagonal
    private Cood scanTree(char[][] view) {

        for (int i = 1; i < view.length - 1; i++) {
            for (int j = 1; j < view.length - 1; j++) {
                if (view[j][i] == 'T') {
                    Cood tempCood = convertCoordinateToAbs(i,j);
                    return createCood(tempCood.getX(),tempCood.getY());
                }
            }
        }
        return null;
    }

//-----------------ITEM SEARCHING-----------------------------------------//

    //Scan the view and return Cood for item
    private boolean searchForItems(char[][] view) {
        Cood moneyCood = null;
        Cood axeCood = null;
        Cood dynamiteCood = null;
        Cood keyCood = null;
        // for every y coordinate
        for (int i = 0; i < 5; i++) {
            // for every x coordinate
            for (int j = 0; j < 5; j++) {
                // if there is an item seen in the view, record the position of that
                if(view[j][i] == '$' || view[j][i] == 'a' || view[j][i] == 'd' || view[j][i] == 'k') {
                    Cood tempCood = convertCoordinateToAbs(i,j);
                    if (view[j][i] == '$') {
                        moneyCood = createCood(tempCood.getX(),tempCood.getY());
                    } else if (view[j][i] == 'a') {
                        axeCood = createCood(tempCood.getX(),tempCood.getY());
                    } else if (view[j][i] == 'd') {
                        dynamiteCood = createCood(tempCood.getX(),tempCood.getY());
                    } else if (view[j][i] == 'k') {
                        keyCood = createCood(tempCood.getX(),tempCood.getY());
                    }
                }
            }
        }
        if (moneyCood != null) {
            if(aStarSearch(moneyCood)) {
                return true;
            }
        }
        if (keyCood != null) {
            if (aStarSearch(keyCood)) {
                return true;
            }
        }
        if (axeCood != null) {
            if(aStarSearch(axeCood)) {
                return true;
            }
        }
        if (dynamiteCood != null) {
            if(aStarSearch(dynamiteCood)) {
                return true;
            }
        }
        return false;
        // DEBUG
        //System.out.println("(" + itemFound.getX() + ", " + itemFound.getY() + ") => " + "(" + view[j][i] + ")");

    }

    private Cood convertCoordinateToAbs(int i, int j) {

        int tempDir = direction;
        while (tempDir != 0) {
            int temp = i;
            i = j;
            j = 4 - temp;
            tempDir = (tempDir + 1) % 4;
        }
        return new Cood(i,j);

    }

    private boolean aStarSearch(Cood destination) {

        // initialize the open list
        Queue<State> open = new PriorityQueue<>();
        // initialize the closed list
        ArrayList<State> closed = new ArrayList<>();
        // put the starting node on the open list (you can leave its f at zero)
        open.add(new State(new Cood(currX, currY),null, 0, 0, true));
        int tempDynamite = dynamite;
//        System.out.println("Destination is: " + map.get(destination));

        // while the open list is not empty
        while(!open.isEmpty()) {
            // pop the node with the least f off the open list
            State currState = open.poll();
//            System.out.println("-->Current State is at: (" + currState.getCurrCood().getX() + "," + currState.getCurrCood().getY() + ") & Fx = " + currState.calculateFx());
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
//                    System.out.println("Found the path to the item at: (" + successor.getCurrCood().getX() + "," + successor.getCurrCood().getY());
                    buildNextMovesToReachItem(successor);
                    return true;
                }
                // calculate g(x)
                successor.calculateGx();
                // calculate h(x)
                successor.calculateHx(destination);
//                System.out.println("Checking successor: (" + successor.getCurrCood().getX() + "," + successor.getCurrCood().getY() + ") & Gx = " + successor.getGx() + " & Hx = " + successor.getHx() + " & Fx = " + successor.calculateFx());
                boolean skipNode = false;
                // if a node with the same position as successor is in the OPEN list \
                // which has a lower f than successor, skip this successor
                for (State checkState : open) {
                    if (checkState.getCurrCood().equals(successor.getCurrCood()) &&
                            successor.calculateFx() > checkState.calculateFx()) {
//                        System.out.println("Denied successor in open list: (" + successor.getCurrCood().getX() + "," + successor.getCurrCood().getY() + ")");
                        skipNode = true;
                    }
                }
                // if a node with the same position as successor is in the CLOSED list \
                // which has a lower f than successor, skip this successor
                for (State checkState : closed) {
                    if (checkState.getCurrCood().equals(successor.getCurrCood()) &&
                            successor.calculateFx() > checkState.calculateFx()) {
//                        System.out.println("Denied successor in closed list: (" + successor.getCurrCood().getX() + "," + successor.getCurrCood().getY() + ")");
                        skipNode = true;
                    }
                }
                // if that tile cannot be traversed on, skip this successor
                if (map.get(destination) == 'T' && isAnObstacle(map.get(successor.getCurrCood()), 0, true)) {
                    skipNode = true;
                } else if(isAnObstacle(map.get(successor.getCurrCood()), tempDynamite, true)) {
//                    System.out.println("Denied successor because cannot go on this tile: (" + successor.getCurrCood().getX() + "," + successor.getCurrCood().getY() + ") & Tile is " + map.get(successor.getCurrCood()) + " & Dynamite is " + tempDynamite);
                    if (map.get(successor.getCurrCood()) == '*' && tempDynamite > 0) {
                        tempDynamite--;
                    } else {
                        skipNode = true;
                    }
                }
                // otherwise, add the node to the open list
                if(!skipNode) {
//                    System.out.println("Added a successor: (" + successor.getCurrCood().getX() + "," + successor.getCurrCood().getY() + ")");
                    open.add(successor);
                }
            }
            // push q on the closed list
            closed.add(currState);
        }
        return false;
    }

    private LinkedList<State> generateSuccessors(State currState) {
        LinkedList<State> successorQueue = new LinkedList<>();
        for(int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                State newState;
                // make sure that the current player position is not recorded as a successor
                if (x == 1 && y == 0) {
                    newState = new State(new Cood(currState.getCurrCood().getX()+x-1, currState.getCurrCood().getY()+1), currState, currState.getGx(), 0, false);
//                    System.out.println("Created successor: (" + newState.getCurrCood().getX() + "," + newState.getCurrCood().getY() + ")");
//                    System.out.println("Successor has tile " + map.get(newState.getCurrCood()));
//                    System.out.println("The view coordinates are: [" + y + "][" + x + "]");
                    successorQueue.add(newState);
                } else if (x == 0 && y == 1) {
                    newState = new State(new Cood(currState.getCurrCood().getX()+x-1, currState.getCurrCood().getY()), currState, currState.getGx(), 0, false);
//                    System.out.println("Created successor: (" + newState.getCurrCood().getX() + "," + newState.getCurrCood().getY() + ")");
//                    System.out.println("Successor has tile " + map.get(newState.getCurrCood()));
//                    System.out.println("The view coordinates are: [" + y + "][" + x + "]");
                    successorQueue.add(newState);
                } else if (x == 2 && y == 1) {
                    newState = new State(new Cood(currState.getCurrCood().getX()+x-1, currState.getCurrCood().getY()), currState, currState.getGx(), 0, false);
//                    System.out.println("Created successor: (" + newState.getCurrCood().getX() + "," + newState.getCurrCood().getY() + ")");
//                    System.out.println("Successor has tile " + map.get(newState.getCurrCood()));
//                    System.out.println("The view coordinates are: [" + y + "][" + x + "]");
                    successorQueue.add(newState);
                } else if (x == 1 && y == 2) {
                    newState = new State(new Cood(currState.getCurrCood().getX()+x-1, currState.getCurrCood().getY()-1), currState, currState.getGx(), 0, false);
//                    System.out.println("Created successor: (" + newState.getCurrCood().getX() + "," + newState.getCurrCood().getY() + ")");
//                    System.out.println("Successor has tile " + map.get(newState.getCurrCood()));
//                    System.out.println("The view coordinates are: [" + y + "][" + x + "]");
                    successorQueue.add(newState);
                }

            }
        }
        return successorQueue;
    }

    private void buildNextMovesToReachItem(State successor) {
        LinkedList<Cood> moveList = new LinkedList<>();
        State currState = successor;
//        System.out.println("The path to get to the item is: ");
        // retrieve all the coordinates that the player has to travel
        while(!currState.isStartingState()) {
            System.out.print("(" + currState.getCurrCood().getX() + "," +currState.getCurrCood().getY() + ") ");
            moveList.add(0, currState.getCurrCood());
            currState = currState.getPrevState();
        }
        System.out.println();
        Cood currPosition = new Cood(currX, currY);
        int currDirection = this.direction;
        // go through the moves
        for (Cood nextPosition : moveList) {
//            System.out.println("Next position is at: (" + nextPosition.getX() + "," + nextPosition.getY() + ")");
            Cood projectedPosition = calculateProjection(currPosition, currDirection);
//            System.out.println("CurrDirection is at: " + currDirection);
//            System.out.println("Testing if matched position is at: (" + projectedPosition.getX() + "," + projectedPosition.getY() + ")");
            while(!projectedPosition.equals(nextPosition)) {
                Cood leftOfPlayer = calculateProjection(currPosition, (currDirection + 4 - 1)%4);
                Cood rightOfPlayer = calculateProjection(currPosition, (currDirection + 1)%4);
                if (isAnObstacle(map.get(leftOfPlayer),dynamite,false) || nextPosition.equals(rightOfPlayer)) {
                    nextMoves.add('r');
                    currDirection = (currDirection + 1)%4;
                } else {
                    nextMoves.add('l');
                    currDirection = (currDirection + 4 - 1)%4;
                }
                projectedPosition = calculateProjection(currPosition, currDirection);
//                System.out.println("CurrDirection is at: " + currDirection);
//                System.out.println("Testing if matched position is at: (" + projectedPosition.getX() + "," + projectedPosition.getY() + ")");
            }
            if (map.get(nextPosition) == 'T') {
                nextMoves.add('c');
            } else if (map.get(nextPosition) == '-') {
                nextMoves.add('u');
            } else if (map.get(nextPosition) == '*') {
                nextMoves.add('b');
            }
            nextMoves.add('f');
            currPosition = nextPosition;
        }
//        System.out.println("The actions to get to the item is: " + nextMoves.toString());
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
        return new Cood(projectedX,projectedY);
    }

//-----------------MAP STITCHING ALGORITHM--------------------------------//

    //Get the absolute cood of each character in the given view
    private void stitchMap(char view[][]) {
        char[][] newView = rotate_view(view, direction);
        // for each y coordinate
        for (int i = 0; i < 5; i++) {
            // for each x coordinate
            for (int j = 0; j < 5; j++) {
                // convert the viewCoordinate to the mapCoordinate
                Cood newCood = createCood(i, j);
                // record the positions of everything in the map
                if (view[j][i] != '\0') {
                    map.put(newCood, newView[j][i]);
                } else {
                    if (onWater) {
                        map.put(newCood, '~');
                    } else {
                        map.put(newCood, ' ');
                    }
                }
            }
        }
        map.put(new Cood(0,0), 'G');
        // DEBUG
        print_map();
    }

    private char[][] rotate_view(char view[][], int times) {
        char newView[][] = view.clone();
        int temp = times;
        System.out.println("direction = " + temp);
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
                rotatedView[j][i] = view[i][4-j];
            }
        }
        return rotatedView;

    }

    private Cood createCood(int x, int y) {

        int newX = x;
        int newY = y;

        newX = newX - 2 + currX;
        if (y == 0) {
            newY = newY + 2 + currY;
        } else if (y == 1) {
            newY = newY + currY;
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

        System.out.print("   ");
        for (int i = smallest; i < largest + 1; i++) {
            String edge = String.format("%1$3s", i);
            System.out.print(edge);
        }
        System.out.println();
        System.out.println("----------------------");
        for (int i = smallest; i < largest + 1; i++) {
            String edge = String.format("%1$3s", i);
            System.out.print(edge + "| ");
            for (int j = smallest; j < largest + 1; j++) {
                Cood accCo = new Cood(j, i);
                if (map.get(accCo) != null){
                    System.out.print(map.get(accCo) + "  ");
                } else {
                    System.out.print("x  ");
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

//-----------------END MAP STITCHING--------------------------------------//

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
        Agent3  agent    = new Agent3();
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
