import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Game {
    private User[] gameBoard = new User[9];
    User currentUser;

    /*
     *  Method: move()
     *  Parameters: The user (player) and the user's spot on board (location)
     *  Return: boolean
     *  Purpose: Error handling for certain actions, marking down the users spot on the board
     *  and switching off the turn to the opponent. Returns true if a move was completed,
     *  false if it was not.
     */
    public synchronized boolean playerMove(int location, User player) {
        if (player != currentUser) {
            return false;
        } else if (player.opponent == null) {
            return false;
        } else if (gameBoard[location] != null) {
            return false;
        } else {
            // Associate the specific location on the board with the user
            gameBoard[location] = currentUser;

            // It's the next players turn
            currentUser = currentUser.opponent;
            return true;
        }
    }

    /*  Class: User
     *  Purpose: Sets up the User class determining who is the user and who is the opponent as well
     *  as setting up their respective sockets
     */
    class User implements Runnable {
        String playerSymbol;
        User opponent;
        Socket socket;
        Scanner inputFromClient;
        PrintWriter outputToClient;

        public User(Socket socket, String symbol) {
            this.socket = socket;
            this.playerSymbol = symbol;
        }

        @Override
        public void run() {
            try {
                setupGame();
                getInputFromPlayer();
            } catch (Exception e) {
                System.out.println("EXCEPTION1: " + e);
            }

            if (opponent != null && opponent.outputToClient != null) {
                opponent.outputToClient.println("Player left");
            }

            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("EXCEPTION2: " + e);
            }
        }

        /*
         *  Method: setupGame()
         *  Parameters: N/a
         *  Return: Void
         *  Purpose: Connects 2 clients together via their respective sockets
         */
        void setupGame() throws IOException {
            inputFromClient = new Scanner(socket.getInputStream());
            outputToClient = new PrintWriter(socket.getOutputStream(), true);
            outputToClient.println("Start " + playerSymbol);

            switch(playerSymbol) {
                case "X":
                    currentUser = this;
                    outputToClient.println("Alert Waiting for opponent to connect");
                    break;
                case "O":
                    opponent = currentUser;
                    opponent.opponent = this;
                    opponent.outputToClient.println("Alert Player connected! Your move");
            }

        }

        /*
         *  Method: getInputFromPlayer()
         *  Parameters: N/a
         *  Return: Void
         *  Purpose: Reads from the data stream input and either quits the game or
         *  makes a move on the board.
         */
        void getInputFromPlayer() {
            while (inputFromClient.hasNext()) {
                String input = inputFromClient.nextLine();
                String inputArray[] = input.split(" ");
                System.out.println(Arrays.toString(inputArray));
                String command = inputArray[0];
                int playersSpot = Integer.valueOf(inputArray[1]);


                if (command.equals("AGAIN")) {
                    return;
                } else if (command.equals("MOVE")) {
                    markPlayersSpot(playersSpot);
                }
            }
        }

        /*
         *  Method: markPlayersSpot()
         *  Parameters: An integer indiciating a user's chosen location
         *  Return: Void
         *  Purpose: Checks to see if a move was valid, if so check for a winner/tie. If the
         *  move was INVALID, alert the player.
         *
         */
        void markPlayersSpot(int boardIndex) {
            if (playerMove(boardIndex, this)) {
                outputToClient.println("Move-P1");
                opponent.outputToClient.println("Move-P2 " + boardIndex);

                checkForWinner();
                checkForTie();
            } else {
                outputToClient.println("Alert ERROR: Invalid move");
            }
        }

        /*
         *  Method: checkForTie()
         *  Parameters: N/a
         *  Return: Void
         *  Purpose: If there are no more spaces left, a tie is outputted to the clients
         */
        void checkForTie() {
            if (noMoreSpacesLeft()) {
                outputToClient.println("Tie");
                opponent.outputToClient.println("Tie");
            }
        }

        /*
         *  Method: noMoreSpacesLeft()
         *  Parameters: N/a
         *  Return: boolean
         *  Purpose: Checks the board for any open spaces to either continue or terminate the game
         */
        boolean noMoreSpacesLeft() {
            for (int i = 0; i < gameBoard.length; i++) {
                if (gameBoard[i] != null) {
                    continue;
                } else {
                    return false;
                }
            }
            return true;
        }

        /*
         *  Method: CheckForWinner()
         *  Parameters: N/a
         *  Return: Void
         *  Purpose: Checks for a winner horizontally, vertically or diagonally
         */
        void checkForWinner() {
            if (checkForHorizontalWins() || checkForVerticalWins() || checkForDiagonalWins()) {
                outputToClient.println("Winner " + currentUser.opponent.playerSymbol);
                opponent.outputToClient.println("Loser " + currentUser.playerSymbol);
            }
        }

        /*
         *  Method: checkForHorizontalWins(), checkForVerticalWins(), checkForDiagonalWins()
         *  Parameters: N/a
         *  Return: boolean
         *  Purpose: Checks for a horizontal/diag./vert. wins in the first, second or third
         *  row/colum/diag
         */
        boolean checkForHorizontalWins() {
            boolean firstRow = false;
            boolean secondRow = false;
            boolean thirdRow = false;

            if (gameBoard[0] != null) {
                firstRow = gameBoard[0] == gameBoard[1] && gameBoard[0] == gameBoard[2];
            }

            if (gameBoard[3] != null) {
                secondRow = gameBoard[3] == gameBoard[4] && gameBoard[3] == gameBoard[5];
            }

            if (gameBoard[6] != null) {
                thirdRow = gameBoard[6] == gameBoard[7] && gameBoard[6] == gameBoard[8];
            }

            return (firstRow || secondRow || thirdRow);
        }

        boolean checkForVerticalWins() {
            boolean firstCol = false;
            boolean secondCol = false;
            boolean thirdCol = false;

            if (gameBoard[0] != null) {
                firstCol = gameBoard[0] == gameBoard[3] && gameBoard[0] == gameBoard[6];
            }

            if (gameBoard[1] != null) {
                secondCol = gameBoard[1] == gameBoard[4] && gameBoard[1] == gameBoard[7];
            }

            if (gameBoard[2] != null) {
                thirdCol = gameBoard[2] == gameBoard[5] && gameBoard[2] == gameBoard[8];
            }

            return (firstCol || secondCol || thirdCol);
        }

        boolean checkForDiagonalWins() {
            boolean firstDiag = false;
            boolean secondDiag = false;

            if (gameBoard[0] != null) {
                firstDiag = gameBoard[0] == gameBoard[4] && gameBoard[0] == gameBoard[8];
            }

            if (gameBoard[2] != null) {
                secondDiag = gameBoard[2] == gameBoard[4] && gameBoard[2] == gameBoard[6];
            }


            return (firstDiag || secondDiag);
        }
    }
}
