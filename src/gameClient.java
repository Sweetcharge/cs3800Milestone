/*
 * Names: Samuel Belarmino, James Domingo, Eugene Nguyen
 * Class: CS3800 - Computer Network
 * Date: April 28th, 2020
 *
 * Purpose: Tic-tac-toe game file that is for Client-side player.
 * 			Contains GUI board setup, user actions from both client and server side,
 * 			and connection to server-side player
 */

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.net.Socket;
import java.io.PrintWriter;
import java.awt.Font;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import javax.swing.*;

public class gameClient {
    private gameBoard currentSquare;
    private gameBoard[] board = new gameBoard[9];

    private JFrame frame = new JFrame("Tic Tac Toe Game!");
    private JLabel instructionalLabel = new JLabel("...");
    private JLabel p1Score = new JLabel("...");
    private JLabel p2Score = new JLabel("...");
    public JPanel playBoard = new JPanel();

    private Socket socket;
    private Scanner scan;
    private PrintWriter printWrite;

    public int score1 = 0;
    public int score2 = 0;

    // Method: gameClient
    // Purpose: initialize socket and creates&initialize playboard
    public gameClient(String serverAddress) throws Exception {
        setupClientNetwork(serverAddress);
        setupPanel(playBoard);
        setupScores(0, 0);
        setupBoard(playBoard);
    }

    public gameClient(String serverAddress, int play1, int play2) throws Exception {
        score1 = play1;
        score2 = play2;
        setupClientNetwork(serverAddress);
        setupPanel(playBoard);
        setupScores(play1, play2);
        setupBoard(playBoard);
    }


    void setupBoard(JPanel playBoard) {
        for (int i = 0; i < board.length; i++) {
            final int j = i;
            board[i] = new gameBoard();
            board[i].addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e)
                {
                    currentSquare = board[j];
                    printWrite.println("MOVE " + j);
                }
            });
            playBoard.add(board[i]);
        }
        frame.getContentPane().add(playBoard, BorderLayout.CENTER);
    }

    void setupPanel(JPanel board) {
        GridLayout grid = new GridLayout(4, 3, 5, 5);

        board.setBackground(Color.BLACK);
        board.setLayout(grid);
        instructionalLabel.setBackground(Color.lightGray);
        instructionalLabel.setHorizontalAlignment(JTextField.CENTER);
        frame.getContentPane().add(instructionalLabel, BorderLayout.SOUTH);

    }

    void setupScores(int p1, int p2) {
        Font font = new Font("Verdana", Font.BOLD, 30);

            p1Score.setText("Player 1: " + p1);
            p2Score.setText("Player 2: " + p2);

            // Horizontal Alignment
            p1Score.setHorizontalAlignment(JTextField.CENTER);
            p2Score.setHorizontalAlignment(JTextField.CENTER);

            // Change player text color
            p1Score.setForeground(Color.ORANGE);
            p2Score.setForeground(Color.LIGHT_GRAY);

            // Change their font
            p1Score.setFont(font);
            p2Score.setFont(font);

            // Position them
            p1Score.setBounds(0, 300, 200, 300);
            p2Score.setBounds(250, 300, 200, 300);

            // Add them to the frame
            frame.getContentPane().add(p1Score);
            frame.getContentPane().add(p2Score);
    }

    void setupClientNetwork(String serverAdd) throws IOException {
        socket = new Socket(serverAdd, 2626);
        scan = new Scanner(socket.getInputStream());
        printWrite = new PrintWriter(socket.getOutputStream(), true);
    }

    //method: Board
    //purpose: GUI tic tac toe board using swing
    static class gameBoard extends JPanel {
        JLabel userSymbol = new JLabel();

        public gameBoard() {
            setBackground(Color.white);
            setLayout(new GridBagLayout());
            userSymbol.setFont(new Font("HelveticaNeue-Thin", Font.BOLD, 80));
            add(userSymbol);
        }

        public void setText(char text) {
            userSymbol.setForeground(text == 'X' ? Color.ORANGE : Color.LIGHT_GRAY);
            userSymbol.setText(text + "");
        }
    }

    //method: play()
    //purpose: method that is active throughout the duration of the game. 
    //         contains numerous user actions and replies.
    public boolean play() throws Exception {
        try {
            //initializes player symbols
            String serverAction = scan.nextLine();
            char p1Symbol = serverAction.charAt(6);
            char p2Symbol = p1Symbol == 'X' ? 'O' : 'X';

            frame.setTitle("Tic Tac Toe: Player " + p1Symbol);

            //While loop continues till victory/defeat/tie/player disconnects
            while (scan.hasNextLine()) {
                serverAction = scan.nextLine();
                String inputArray[] = serverAction.split(" ");
                String command = inputArray[0];

                if (command.equals("Move-P1")) {
                    instructionalLabel.setText("Not your turn");
                    currentSquare.setText(p1Symbol);
                    currentSquare.repaint();
                }
                else if (command.equals("Move-P2")) {
                    int location = Integer.parseInt(serverAction.substring(8));
                    board[location].setText(p2Symbol);
                    board[location].repaint();
                    instructionalLabel.setText("Your turn");
                }
                else if (command.equals("Alert")) {
                    instructionalLabel.setText(serverAction.substring(6));
                }
                else if (command.equals("Winner")) {
                    JOptionPane.showMessageDialog(frame, "You are the winner!");
                    if (inputArray[1].equals("X")) {
                        score1 += 1;
                    } else {
                        score2 += 1;
                    }
                    break;
                }
                else if (command.equals("Loser")) {
                    JOptionPane.showMessageDialog(frame, "GGs only");
                    if (inputArray[1].equals("X")) {
                        score2 += 1;
                    } else {
                        score1 += 1;
                    }
                    break;
                }
                else if (command.equals("Tie")) {
                    JOptionPane.showMessageDialog(frame, "You didn't lose! But you also didn't win...");
                    break;
                }
                else if (command.equals("Player")) {
                    JOptionPane.showMessageDialog(frame, "Opponent left/disconnected.");
                    break;
                }
            }
            printWrite.println("AGAIN");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            socket.close();
            frame.dispose();
            return true;
        }
    }

    //method: main
    //purpose: starts gameClient
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Pass the server IP as the sole command line argument");
            return;
        }

        gameClient clientPlayer = new gameClient(args[0]);
        clientPlayer.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        clientPlayer.frame.setSize(500, 550);
        clientPlayer.frame.setVisible(true);
        clientPlayer.frame.setResizable(false);
        clientPlayer.play();

        while (clientPlayer.play()) {
            System.out.println("PLAYER 1: " + clientPlayer.score1);
            System.out.println("PLAYER 2: " + clientPlayer.score2);
            clientPlayer = new gameClient(args[0], clientPlayer.score1, clientPlayer.score2);
            clientPlayer.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            clientPlayer.frame.setSize(500, 550);
            clientPlayer.frame.setVisible(true);
            clientPlayer.frame.setResizable(false);
            clientPlayer.play();
        }

    }
}