import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.*;


public class TicTacToe implements Runnable {

    private String ip = "localhost";
    private int port = 22222;
    private Scanner scanner = new Scanner(System.in);

    // Dimensions
    private JFrame frame;
    private final int WIDTH = 500;
    private final int HEIGHT = 630;

    // Network services
    private Thread thread;
    private Socket socket;
    private DataOutputStream outStream;
    private DataInputStream inStream;
    private ServerSocket serverSocket;

    // Game variables
    private BufferedImage gameBoard;
    private BufferedImage p1X;
    private BufferedImage p1O;
    private BufferedImage p2X;
    private BufferedImage p2O;

    private String[] spaces = new String[9];

    private boolean yourTurn = false;
    private boolean circle = true;
    private boolean accepted = false;
    private boolean noCommWithPlayer = false;
    private boolean won = false;
    private boolean loser = false;
    private boolean tie = false;

    private int lengthOfSpace = 160;
    private int err = 0;
    private int firstSpot = -1;
    private int secondSpot = -1;

    private Font font = new Font("Verdana", Font.BOLD, 32);
    private Font smallFont = new Font("Verdana", Font.BOLD, 20);
    private Font bigFont = new Font("Verdana", Font.BOLD, 50);

    private String waiting = "Waiting to connect with another player";
    private String failedConnection = "Unable to connect with player";
    private String wonString = "Winner winner!";
    private String loserString = "You lost, try again";
    private String tieString = "Tie game, run it back";

    // Graphics
    private Painter painter;

    public TicTacToe() throws IOException{
        System.out.println("Enter an IP: ");
        ip = scanner.nextLine();
        System.out.println("Enter the port number: ");
        port = scanner.nextInt();

        while (port < 1 || port > 66535) {
            System.out.println("Invalid port! Please enter a port in between 1 and 66535");
            port = scanner.nextInt();
        }

        loadImages();

        painter = new Painter();
        painter.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        frame = new JFrame();
        frame.setTitle("TicTacToe");
        frame.setContentPane(painter);
        frame.setSize(WIDTH, HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private void loadImages() throws IOException {
        try {
            File boardFile = new File("src/board.png");
            File p1XFile = new File("src/redX.png");
            File p1OFile = new File("src/redCircle.png");
            File p2XFile = new File("src/blueX.png");
            File p2OFile = new File("src/blueCircle.png");

            gameBoard = ImageIO.read(boardFile);
            p1X = ImageIO.read(p1XFile);
            p1O = ImageIO.read(p1OFile);
            p2X = ImageIO.read(p2XFile);
            p2O = ImageIO.read(p2OFile);

        } catch (IOException e) {
            System.out.println(e);
        }
    }

    @Override
    public void run() {

    }

    public class Painter extends JPanel implements MouseListener {
        private static final long serialVersionUID = 1L;

        public Painter() {
            setFocusable(true);
            requestFocus();
            setBackground(Color.white);
            addMouseListener(this);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            render(g);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (accepted) {
                if (yourTurn && !noCommWithPlayer && !won && !loser) {
                    int x = e.getX() / lengthOfSpace;
                    int y = e.getY() / lengthOfSpace;
                    y *= 3;
                    int position = x + y;

                    if (spaces[position] == null) {
                        if (!circle) spaces[position] = "X";
                        else spaces[position] = "O";
                        yourTurn = false;
                        repaint();
                        Toolkit.getDefaultToolkit().sync();

                        try {
                            outStream.writeInt(position);
                            outStream.flush();
                        } catch (IOException e1) {
                            err++;
                            e1.printStackTrace();
                        }

                        System.out.println("DATA WAS SENT");
//                        checkForWin();
//                        checkForTie();

                    }
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    public void setupScores(Graphics g, int p1Score, int p2Score) {
        String player1String = "Player 1: " + p1Score;
        String player2String = "Player 2: " + p2Score;

        // Player 1 graphics
        g.setColor(Color.BLUE);
        g.setFont(font);
        Graphics2D p1Graphics = (Graphics2D) g;
        p1Graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.drawString(player1String, 5, 600);

        // Player 2 graphics
        g.setColor(Color.PINK);
        g.setFont(font);
        Graphics2D p2Graphics = (Graphics2D) g;
        p2Graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.drawString(player2String, 300, 600);
    }

    public void render(Graphics g) {
        g.drawImage(gameBoard, 0, 0, null);
        int player1Score = 0;
        int player2Score = 0;

        setupScores(g, player1Score, player2Score);

        if (noCommWithPlayer) {
            System.out.println("No communication with player!");
            g.setColor(Color.RED);
            g.setFont(smallFont);
            Graphics2D g2 = (Graphics2D) g;
            int stringWidth = g2.getFontMetrics().stringWidth(failedConnection);
            g.drawString(failedConnection, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
            return;
        }

        if (accepted) {
            for (int i = 0; i < spaces.length; i++) {
                if (spaces[i] != null) {
                    if (spaces[i].equals("X")) {
                        if (circle) {
                            g.drawImage(p1X, (i % 3) * lengthOfSpace + 10 * (i % 3), (int) (i / 3) * lengthOfSpace + 10 * (int) (i / 3), null);
                        } else {
                            g.drawImage(p2X, (i % 3) * lengthOfSpace + 10 * (i % 3), (int) (i / 3) * lengthOfSpace + 10 * (int) (i / 3), null);
                        }
                    } else if (spaces[i].equals("O")) {
                        if (circle) {
                            g.drawImage(p1O, (i % 3) * lengthOfSpace + 10 * (i % 3), (int) (i / 3) * lengthOfSpace + 10 * (int) (i / 3), null);
                        } else {
                            g.drawImage(p2O, (i % 3) * lengthOfSpace + 10 * (i % 3), (int) (i / 3) * lengthOfSpace + 10 * (int) (i / 3), null);
                        }
                    }
                }
            }

            if (won || loser) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setStroke(new BasicStroke(10));
                g.setColor(Color.BLACK);
                g.drawLine(firstSpot % 3 * lengthOfSpace + 10 * firstSpot % 3 + lengthOfSpace / 2, (int) (firstSpot / 3) * lengthOfSpace + 10 * (int) (firstSpot / 3) + lengthOfSpace / 2, secondSpot % 3 * lengthOfSpace + 10 * secondSpot % 3 + lengthOfSpace / 2, (int) (secondSpot / 3) * lengthOfSpace + 10 * (int) (secondSpot / 3) + lengthOfSpace / 2);

                g.setColor(Color.RED);
                g.setFont(bigFont);
                if (won) {
                    int stringWidth = g2.getFontMetrics().stringWidth(wonString);
                    g.drawString(wonString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
                } else if (loser) {
                    int stringWidth = g2.getFontMetrics().stringWidth(loserString);
                    g.drawString(loserString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
                }
            }

            if (tie) {
                Graphics2D g2 = (Graphics2D) g;
                g.setColor(Color.BLACK);
                g.setFont(bigFont);
                int stringWidth = g2.getFontMetrics().stringWidth(tieString);
                g.drawString(tieString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
            }
        } else {
            g.setColor(Color.RED);
            g.setFont(smallFont);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int stringWidth = g2.getFontMetrics().stringWidth(failedConnection);
            g.drawString(failedConnection, WIDTH / 2 - stringWidth / 2, 527 / 2);
        }
    }

    public static void main(String[] args) throws IOException {
        TicTacToe ttt = new TicTacToe();
    }
}
