/*********************************************
 *  Agent.java
 *  Agent for Text-Based Adventure Game
 *  COMP3411 Artificial Intelligence
 *  UNSW Session 1, 2017
 *  By Andrew Ha and George Chieng
 */

import java.util.*;
import java.io.*;
import java.net.*;

    /*
    Briefly describe how your program works, including any algorithms and data structures employed, and explain any design decisions you made along the way.
    Our program first starts off by stitching together a map given the view.
    To do this, we rotate the view until it is upright (according to the orientation of the player when starting the game).
    This is then stiched by updating a hash map which given a coordinate, it returns the property of that square.
    We decided to use a coordinate class in order to access the values of the hashmap more easier.
    We initially started off by using a 2d array. However, we came to a constraint where we couldn't predict how big the map is, resulting in array out of bounds error.
    The program then proceeds to the phase where it decides the next action.
    We use a decision tree to determine this, with many if statements depending on the situation.
    We first determine whether there is an item nearby. If there is an item, we use an A* search to try to get to the item (or tree), given the available resources.
    When we get to the item, we have variables which record whether we have have the item or have used it up.
    In the A* search we use the Manhattan heuristic to determine the fastest route to the destination.
    When the A* search finishes, we then convert the path of coordinates to moves which the player takes.
    We implemented a queue which holds these moves and execute the moves before doing other actions.
    If the searches fail, we then start to wander around.
    We first thought about hugging the wall forever. However, this doesn't work for all cases.
    The solution we implemented is a modified version of the hug.
    The player first wanders randomly until it reaches a corner.
    It then assumes that the corner is the boundary of the map and start hugging.
    Once the AI realises that there is space beyond that corner, it will start roaming randomly again.
    We finally account for any changes to the player when doing a move (like fixing the direction when rotating or updating variables when going to an item)
    */

public class Agent {

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

    public Agent() {
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
    }

    public char get_action( char view[][] ) {
        //-----------------STEPS BEFORE DETERMINING PLAYER ACTION-----------------//
        // stitches the map
        stitchMap(view);
        // if the player found the goal and needs a way to go home, queue a list to go home
        if (gold && nextMoves.isEmpty()) {
            aStarSearch(new Cood(0, 0));
        }

        //-----------------DETERMINING PLAYER ACTION------------------------------//
        // default action is to move forward
        char action = 'f';
        // if there are a list of moves to travel, then continue with the steps
        if (!nextMoves.isEmpty()) {
            action = nextMoves.poll();
            // else try to find something to do
        } else {
            // try to find a way to a visible item
            boolean canGetItem = searchForItems(view);
            // try to find a way to go cut a tree
            boolean canCutTree = false;
            Cood nearbyTree = scanTree(view);
            // if you can find a nearby tree and have an axe and there's no items nearby
            if (nearbyTree != null && axe && !canGetItem) {
                // if there is a viable path to the tree
                if (aStarSearch(nearbyTree)) {
                    // mark a boolean to start going to cut the tree
                    canCutTree = true;
                }
            }
            // if you found a path to an item or a
            if (canGetItem || canCutTree) {
                action = nextMoves.poll();
            } else {
                // if the player is hugging the walls
                if (isHuggingWall) {
                    // if the player is hugging the left walls
                    if (hugSide == 'l') {
                        // if the player was previously hugging a wall and reached an inner corner, rotate left and go forward
                        if (isAnObstacle(view[3][1], dynamite, false) && view[2][1] == ' ') {
                            action = 'l';
                            nextMoves.add('f');
                            // if the player reaches a corner which forces the player to go right, rotate right
                        } else if (isAnObstacle(view[2][1], dynamite, false) && isAnObstacle(view[1][2], dynamite, false)) {
                            action = 'r';
                            // if the player reaches a corner which forces the player to go left, rotate left and go forward
                        } else if (isAnObstacle(view[2][3], dynamite, false) && isAnObstacle(view[1][2], dynamite, false)) {
                            action = 'l';
                            nextMoves.add('f');
                            // if the player is hugging the left hand side of the wall, keep going forward
                        } else if (view[1][2] == ' ' && isAnObstacle(view[2][1], dynamite, false)) {
                            action = 'f';
                        }

                        // If the player is hugging the right
                    } else if (hugSide == 'r') {
                        // if the player was previously hugging a wall and reached an inner corner, rotate left and go forward
                        if (isAnObstacle(view[3][3], dynamite, true) && view[2][3] == ' ') {
                            action = 'r';
                            nextMoves.add('f');
                            // if the player reaches a corner which forces the player to go left, rotate left
                        } else if (isAnObstacle(view[2][3], dynamite, false) && isAnObstacle(view[1][2], dynamite, false)) {
                            action = 'l';
                            // if the player reaches a corner which forces the player to go right, rotate right and go forward
                        } else if (isAnObstacle(view[2][1], dynamite, false) && isAnObstacle(view[1][2], dynamite, false)) {
                            action = 'r';
                            nextMoves.add('f');
                            // if the player is hugging the left hand side of the wall, keep going forward
                        } else if (view[1][2] == ' ' && isAnObstacle(view[2][3], dynamite, false)) {
                            action = 'f';
                        }
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
            }
        }

//-----------------STEPS AFTER DETERMINING PLAYER ACTION------------------//
        // if the player's next action is to move forward
        if (action == 'f') {
            // if the square in front is the money
            if (view[1][2] == '$') {
                // update the boolean to true
                gold = true;
                // update the hash so the map no longer has the money icon
                for (Cood i : map.keySet()) {
                    if (map.get(i) == '$') {
                        map.put(i, ' ');
                        break;
                    }
                }
                // if the square in front is an axe, set boolean to true
            } else if (view[1][2] == 'a') {
                axe = true;
                // if the square in front is dynamite, increase the amount of dynamite by 1
            } else if (view[1][2] == 'd') {
                dynamite++;
                // if the square in front is a key, set boolean to true
            } else if (view[1][2] == 'k') {
                key = true;
            }
            updateCurrPosition();
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
            // if the next move is to bomb a wall
        } else if (action == 'b') {
            dynamite--;
        }

        return action;
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
        // take dynamite into consideration if the method is called in the a star search
        if (isSearch) {
            return ((c == '~' && !wood) || (c == '*' && dynamites == 0) || (c == 'T' && !axe) || c == '.' || (c == '-' && !key));
        } else {
            return ((c == '~' && !wood) || (c == '*') || (c == 'T' && !axe) || c == '.' || (c == '-' && !key));
        }
    }

    //When met with an obstacle rotate
    private char rotateAtAnObstacle(char view[][]) {
        char action;
        if (isAnObstacle(view[2][1], dynamite, false)) {
            action = 'r';
        } else {
            action = 'l';
        }
        return action;
    }

    //Scan the if there exist a tree in the view
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
        // try to go to the money first, then key, then axe, then dynamite
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
    }

    // converts a coordinate from a view to the absolute coordinate in the stitched map
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

    // do the a star search
    private boolean aStarSearch(Cood destination) {

        // initialize the open list
        Queue<State> open = new PriorityQueue<>();
        // initialize the closed list
        ArrayList<State> closed = new ArrayList<>();
        // put the starting node on the open list (you can leave its f at zero)
        open.add(new State(new Cood(currX, currY),null, 0, 0, true));
        int tempDynamite = dynamite;

        // while the open list is not empty
        while(!open.isEmpty()) {
            // pop the node with the least f off the open list
            State currState = open.poll();
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
                            successor.calculateFx() > checkState.calculateFx()) {
                        skipNode = true;
                    }
                }
                // if a node with the same position as successor is in the CLOSED list \
                // which has a lower f than successor, skip this successor
                for (State checkState : closed) {
                    if (checkState.getCurrCood().equals(successor.getCurrCood()) &&
                            successor.calculateFx() > checkState.calculateFx()) {
                        skipNode = true;
                    }
                }
                // if that tile cannot be traversed on, skip this successor
                if (map.get(destination) == 'T' && isAnObstacle(map.get(successor.getCurrCood()), 0, true)) {
                    skipNode = true;
                } else if(isAnObstacle(map.get(successor.getCurrCood()), tempDynamite, true)) {
                    if (map.get(successor.getCurrCood()) == '*' && tempDynamite > 0) {
                        tempDynamite--;
                    } else {
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

    private LinkedList<State> generateSuccessors(State currState) {
        LinkedList<State> successorQueue = new LinkedList<>();
        for(int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                State newState;
                // make successors for the squares around the player
                if (x == 1 && y == 0) {
                    newState = new State(new Cood(currState.getCurrCood().getX()+x-1, currState.getCurrCood().getY()+1), currState, currState.getGx(), 0, false);
                    successorQueue.add(newState);
                } else if (x == 0 && y == 1) {
                    newState = new State(new Cood(currState.getCurrCood().getX()+x-1, currState.getCurrCood().getY()), currState, currState.getGx(), 0, false);
                    successorQueue.add(newState);
                } else if (x == 2 && y == 1) {
                    newState = new State(new Cood(currState.getCurrCood().getX()+x-1, currState.getCurrCood().getY()), currState, currState.getGx(), 0, false);
                    successorQueue.add(newState);
                } else if (x == 1 && y == 2) {
                    newState = new State(new Cood(currState.getCurrCood().getX()+x-1, currState.getCurrCood().getY()-1), currState, currState.getGx(), 0, false);
                    successorQueue.add(newState);
                }

            }
        }
        return successorQueue;
    }

    private void buildNextMovesToReachItem(State successor) {
        // list of coordinates that the player must go to get from curr position to destination
        LinkedList<Cood> moveList = new LinkedList<>();
        State currState = successor;
        // retrieve all the coordinates that the player has to travel
        while(!currState.isStartingState()) {
            moveList.add(0, currState.getCurrCood());
            currState = currState.getPrevState();
        }
        Cood currPosition = new Cood(currX, currY);
        int currDirection = this.direction;
        // go through the coordinates until a path is made to the solution
        for (Cood nextPosition : moveList) {
            // find out where the player would go if they went forward in the direction being faced
            Cood projectedPosition = calculateProjection(currPosition, currDirection);
            // keep rotating the player until the player can go forward to the desired position
            while(!projectedPosition.equals(nextPosition)) {
                Cood leftOfPlayer = calculateProjection(currPosition, (currDirection + 4 - 1)%4);
                Cood rightOfPlayer = calculateProjection(currPosition, (currDirection + 1)%4);
                // rotate right if there is a wall on the left of the player or the player only needs to go right to reach the desired position
                if (isAnObstacle(map.get(leftOfPlayer),dynamite,false) || nextPosition.equals(rightOfPlayer)) {
                    nextMoves.add('r');
                    currDirection = (currDirection + 1)%4;
                } else {
                    nextMoves.add('l');
                    currDirection = (currDirection + 4 - 1)%4;
                }
                // calculate the new projected position once the rotation has been done
                projectedPosition = calculateProjection(currPosition, currDirection);
            }
            // if the next coordinate is a tree, cut the tree
            if (map.get(nextPosition) == 'T') {
                nextMoves.add('c');
                // if the next coordinate is a door, open the door
            } else if (map.get(nextPosition) == '-') {
                nextMoves.add('u');
                // if the next position is the wall, destroy the wall
            } else if (map.get(nextPosition) == '*') {
                nextMoves.add('b');
            }
            // move forward to that next position
            nextMoves.add('f');
            // now move forward and keep going
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
        Agent  agent    = new Agent();
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
