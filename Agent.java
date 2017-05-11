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
<<<<<<< HEAD

    public char get_action(char view[][]) {
        //testing functionality
//        int ch = 0;
//        System.out.print("Enter Action(s): ");
=======
    
   private List<State> stateList = new ArrayList<State>();
   private char[][] exploredMap;
   
   public char get_action( char view[][] ) {
<<<<<<< HEAD

       char action;
       
       // create a newState
       State newState;
       if (stateList.isEmpty()) {
           newState = new State(false, view);
       } else {
           State prevState = stateList.get(stateList.size() - 1);
           newState = new State(prevState.getHasKey(), view);
       }
       
       if (newState.getHasKey()) {
           buildExploredMap();
       }
       
       if (view[1][2] == '~') {
           action = 'r';
       } else if (view[1][2] == '$') {
           newState.setHasKey(true);
           action = 'f';
       } else {
           action = 'f';
           
       }

       stateList.add(newState);
       
       return action;
       
   }

   private void buildExploredMap() {
    
       // the minimum and maximum values which the 
       int xMin, xMax, yMin, yMax, xCurr, yCurr = 0;
       
       for (State checkState : stateList) {
           if () {
               
           } else if () {
               
           } else if () {
               
           } else if () {
               
           }
       }
=======
       //testing functionality
       return ('f');
>>>>>>> 4a93b83e18a6d786b7dc4e1ff39b00666e9619f6
//
//        try {
//            while (ch != -1) {
//                // read character from keyboard
//                ch = System.in.read();
//
//                switch (ch) { // if character is a valid action, return it
//                    case 'F':
//                    case 'L':
//                    case 'R':
//                    case 'C':
//                    case 'U':
//                    case 'B':
//                    case 'f':
//                    case 'l':
//                    case 'r':
//                    case 'c':
//                    case 'u':
//                    case 'b':
//                        return ((char) ch);
//                }
//            }
<<<<<<< HEAD
//        } catch (IOException e) {
//            System.out.println("IO error:" + e);
//        }
    }

    void print_view(char view[][]) {
        int i, j;
=======
//         }
//      }
//      catch (IOException e) {
//         System.out.println ("IO error:" + e );
//      }
>>>>>>> baf4df538ed584214e585a49de46481485155fc5
   }

void print_view( char view[][] ) {
      int i,j;
>>>>>>> 4a93b83e18a6d786b7dc4e1ff39b00666e9619f6

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
