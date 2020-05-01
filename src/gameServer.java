import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class gameServer {
    public static void main(String[] args) throws IOException {
        try {
            ServerSocket socket = new ServerSocket(2626);
            System.out.println("Server running...");
            ExecutorService pool = Executors.newFixedThreadPool(200);
            while (true) {
                Game game = new Game();
                pool.execute(game.new User(socket.accept(), "X"));
                pool.execute(game.new User(socket.accept(), "O"));
                System.out.println("SERVER DONE");
            }
        } catch (IOException e) {
            System.out.println("ERROR: " + e);
        }
    }
}
