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
    private int direction;
    private int currX;
    private int currY;
    private boolean isHugging;

    //Inventory
    private boolean axe;
    private boolean key;
    private boolean dynamite;
    private boolean gold;
    private boolean wood;

    public Agent2() {
        this.map = new HashMap<>();
        this.nextMoves = new LinkedList<>();
        this.direction = 0;
        this.currX = 0;
        this.currY = 0;
        this.isHugging = false;
        axe = false;
        key = false;
        dynamite = false;
        gold = false;
        wood = false;
    }

    public char get_action( char view[][] ) {

//-----------------ACTIONS BEFORE DETERMINING ACTION-----------------//

        stitchMap(view);

//---------------------------DETERMINING ACTION-----------------//

        // default action is to go forward
        char action = 'f';

        // if there are a list of moves to travel, then continue with the steps
        if (!nextMoves.isEmpty()) {
            action = nextMoves.poll();
        // else try to find something to do
        } else {
            if (isHugging) {
                // if we hit an obstacle, then turn
                if (view[1][2] == '~' || view[1][2] == '*' || view[1][2] == 'T' || view[1][2] == '.') {
                    action = rotateAtAnObstacle(view);

                    // else if we're no longer touching a wall, turn the other way
                } else if (view[2][1] == ' ') {
                    action = 'l';
                    nextMoves.add('f');
                }
                // else just start roaming until we hit an obstacle
            } else {
                // if we hit an obstacle, start hugging obstacles
                if (view[1][2] == '~' || view[1][2] == '*' || view[1][2] == 'T' || view[1][2] == '.') {
                    action = rotateAtAnObstacle(view);
                    isHugging = true;
                }
            }
        }

//-----------------ACTIONS AFTER DETERMINING ACTION-----------------//
        //This snippet is so that AI isn't an idiot and jump into the water or go into the forest
        if (action == 'f' && (view[1][2] == '~' || view[1][2] == '.')) {
            double coinflip = Math.random() % 2;
            if (coinflip == 1) {
                action = 'l';
            } else {
                action = 'r';
            }
        }

        // update the coordinate
        if (action == 'f') {
            updateCurrPosition();
        } else if (action == 'l') {
            direction = (direction + 4 - 1) % 4;
        } else if (action == 'r') {
            direction = (direction + 4 + 1) % 4;
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
                    System.out.println("True...");
                }
            }
        }
        if (treeExist == true) {
            if ((2 - treePosX) == 1 || (2 - treePosX) == -1 || (2 - treePosY) == 1 || (2 - treePosY) == -1) {
                System.out.println("gonna cut");
                if (treePosX == 1 && treePosY == 2) {
                    nextMoves.add('c');
                    wood = true;
                } else if (treePosX == 2 && treePosY == 1) {
                    nextMoves.add('l');
                    nextMoves.add('c');
                } else if (treePosX == 3 && treePosY == 2) {
                    nextMoves.add('r');
                    nextMoves.add('c');
                } else if (treePosX == 2 && treePosY == 3) {
                    nextMoves.add('r');
                    nextMoves.add('r');
                    nextMoves.add('c');
                }
//            } else {
//                System.out.print("nah");
//                walkTowardsTree(treePosX, treePosY);
            }
        }
    }

    //put a set of move if item is right next to AI
    private void getItem(char[][] view) {
        int itemPosX;
        int itemPosY;
        for (int i = 0; i < view.length; i++) {
            for (int j = 0; j < view.length; j++) {
                if (view[i][j] == 'a' || view[i][j] == '$' || view[i][j] == 'd' || view[i][j] == 'k') {
                    itemPosX = i;
                    itemPosY = j;
                    if (view[1][2] == 'a') {
                        axe = true;
                    } else if (view[1][2] == '$') {
                        gold = true;
                    } else if (view[1][2] == 'd') {
                        dynamite = true;
                    } else if (view[1][2] == 'k') {
                        key = true;
                    }
                    if ((2 - itemPosX) == 1 || (2 - itemPosX) == -1 || (2 - itemPosY) == 1 || (2 - itemPosY) == -1) {
                        if (itemPosX == 1 && itemPosY == 2) {
                            nextMoves.add('f');
                        } else if (itemPosX == 2 && itemPosY == 1) {
                            nextMoves.add('l');
                        } else if (itemPosX == 3 && itemPosY == 2) {
                            nextMoves.add('r');
                        } else if (itemPosX == 2 && itemPosY == 3) {
                            nextMoves.add('r');
                            nextMoves.add('r');
                        }
                    }
                }
            }
        }
    }

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
                    map.put(newCood, ' ');
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

    // NO LONGER OUR CODE

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
