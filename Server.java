import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Server implements Runnable {
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;
    private int targetNumber;   // The number both clients will try to guess
    private boolean gameStarted = false;

    public Server() {
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(8080);
            pool = Executors.newCachedThreadPool();

            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);

                // Start the game when the second client connects
                if (connections.size() == 2 && !gameStarted) {
                    startNewGame();
                }
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    private void startNewGame() {
        Random rand = new Random();
        targetNumber = rand.nextInt(100) + 1; // Random number between 1 and 100
        gameStarted = true;
        broadcast("Game started! Guess the number between 1 and 100.");
    }

    public void broadcast(String message) {
        synchronized (connections) {
            for (ConnectionHandler c : connections) {
                if (c != null) {
                    c.sendMessage(message);
                }
            }
        }
    }

    public void shutdown() {
        done = true;
        try {
            if (server != null && !server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler c : connections) {
                c.shutdown();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ConnectionHandler implements Runnable {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String username;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                out.println("Please enter a Username:");
                username = in.readLine();
                if (username == null) {
                    shutdown();
                    return;
                }

                System.out.println(username + " connected");
                broadcast(username + " has joined the game!");

                String message;
                while ((message = in.readLine()) != null && gameStarted) {
                    try {
                        int guess = Integer.parseInt(message.trim());
                        if (guess < targetNumber) {
                            out.println("Too low! Try again.");
                        } else if (guess > targetNumber) {
                            out.println("Too high! Try again.");
                        } else {
                            broadcast("Congratulations, " + username + " guessed the number " + targetNumber + " correctly!");
                            gameStarted = false; // End the game
                            startNewGame(); // Optionally start a new game
                            break;
                        }
                    } catch (NumberFormatException e) {
                        out.println("Please enter a valid number.");
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void shutdown() {
            try {
                if (!client.isClosed()) {
                    in.close();
                    out.close();
                    client.close();
                }
                synchronized (connections) {
                    connections.remove(this);
                    if (connections.size() < 2) {
                        gameStarted = false; // Reset game if players disconnect
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        Thread serverThread = new Thread(server);
        serverThread.start();
    }
}
