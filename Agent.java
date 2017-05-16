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
    private State currState = new State(false, null);
    private char prevMove = 'Q';
    private int rotation  = 0;

    public char get_action(char view[][]) {
        char action = 'f';
//        State newState;
//        if (stateList.isEmpty()) {
//            newState = new State(false, view);
//        } else {
//            State prevState = stateList.get(stateList.size() - 1);
//            newState = new State(prevState.getHasKey(), view);
//        }
//
//        if (newState.getHasKey()) {
//
//        }
//
//        if (view[1][2] == '~') {
//            action = 'r';
//        } else if (view[1][2] == '$') {
//            newState.setHasKey(true);
//            action = 'f';
//        } else {
//            action = 'f';
//        }
//
//        stateList.add(newState);

        return action;
    }

    private void update_map(State curr, char[][] currView){
        if (curr.getView() == null){
            curr.setView(currView);
            curr.setHasGold(find_gold(currView));
        } else {
            if (prevMove == 'f'){
                char[][] newMap = stitch_map(currView, rotate_view(curr.getView(), rotation), rotation, prevMove);
                curr.setView(newMap);
                curr.setHasGold(find_gold(curr.getView()));
            }
        }
    }


    private char[][] stitch_map(char curr[][], char prev_map[][], int times, char prev){
        // should we make sure the map is always square or can it be varying dimension???
        char[][] newMap = new char[curr.length+1][curr.length+1]
        if (prev == 'f'){
            if (times == 0){
                //draws the first row with prev map's first row
            } else if (times == 1){
                //draws the last col ...
            } else if (times == 2){
                //draws the last row ...
            } else if (times == 3){
                //draws the first col ...
            }
        } else {
            return curr;
        }
        return newMap;
    }

    //scan through the current view and gve back a boolean value
    private boolean find_gold (char view[][]){
        boolean gold = false;
        for (int i = 0; i < view.length; i++) {
            for (int j = 0; j < view.length; j++) {
                if (view[i][j] == '$'){
                    gold = true;
                }
            }
        }
        return gold;
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
