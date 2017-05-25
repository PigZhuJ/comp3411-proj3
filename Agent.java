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

    private HashMap<Cood, Character> map = new HashMap<Cood, Character>();
    private Integer currX = 0;
    private Integer currY = 0;
    private int direction = 0;

    public char get_action(char view[][]) {

        // default action is to go forward
        char action = 'f';

        // stitch the map given the view
        stitchMap(view);

        if(view[1][2] == '~' || view[1][2] == '*' || view[1][2] == 'T') {
            action = 'r';
            direction = (direction + 1) % 4;
        }

        if (action == 'f') {
            updateCurrPosition();
        }

        System.out.println("(" + currX + ", " + currY + ")");
        print_map();


        return action;

    }

    public void stitchMap(char view[][]) {
        char[][] newView = rotate_view(view, direction);
        for (int x = 0; x < view.length; x++) {
            for (int y = 0; y < view.length; y++) {
                Cood newCood = new Cood((currX + x -2), (currY + y - 2));
                if (map.get(newCood) == null){
                    map.put(newCood, newView[x][y]);
//                    System.out.print(map.get(newCood));
                }
            }
            System.out.println();
        }
    }

    //Rotate the view to 0 degree
    private char[][] rotate_view (char view[][], int times){
        char newView[][] = new char[view.length][view.length];
        while ((times % 4) != 0){
            if (times < 0){
                newView = clockwise(view);
                times++;
            } else if (times > 0){
                newView = anticlockwise(view);
                times--;
            }
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
        System.out.println(sX + ", " + sY);
        System.out.println(lX + ", " + lY);
        Cood accCo = new Cood(1, 2);
        System.out.print(map.get(accCo));

//        for (int i = sX; i < lX + 1; i++) {
//            for (int j = sY; j < lY + 1; j++) {
//                Cood accCo = new Cood(i, j);
//                System.out.print(map.get(accCo));
//            }
//            System.out.println();
//        }
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
        System.out.println("\n+-----+");
        for (i = 0; i < 5; i++) {
            System.out.print("|");
            for (j = 0; j < 5; j++) {
                if ((i == 2) && (j == 2)) {
                    System.out.print('^');
                } else {
                    System.out.print(view[i][j]);
                }
            }
            System.out.println("|");
        }
        System.out.println("+-----+");
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
