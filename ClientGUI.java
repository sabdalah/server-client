

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientGUI extends JFrame implements Runnable {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private JTextField usernameField, guessField;
    private JTextArea messageArea;
    private JButton connectButton, guessButton;
    private boolean connected = false;

    public ClientGUI(String serverIp, int serverPort) {
        // Setting up the GUI layout
        setTitle("Guess the Number - Client");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Message area to display game updates
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        add(new JScrollPane(messageArea), BorderLayout.CENTER);

        // Panel for username input and connection
        JPanel topPanel = new JPanel(new BorderLayout());
        usernameField = new JTextField("Enter username");
        connectButton = new JButton("Connect");
        topPanel.add(usernameField, BorderLayout.CENTER);
        topPanel.add(connectButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Panel for guesses and guess button
        JPanel bottomPanel = new JPanel(new BorderLayout());
        guessField = new JTextField("Enter your guess");
        guessButton = new JButton("Guess");
        bottomPanel.add(guessField, BorderLayout.CENTER);
        bottomPanel.add(guessButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // Disable guess input until connected
        guessField.setEnabled(false);
        guessButton.setEnabled(false);

        // Action listeners for buttons
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!connected) {
                    String username = usernameField.getText().trim();
                    if (!username.isEmpty()) {
                        connectToServer(serverIp, serverPort, username);
                    } else {
                        JOptionPane.showMessageDialog(null, "Please enter a valid username.");
                    }
                }
            }
        });

        guessButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String guess = guessField.getText().trim();
                if (connected && !guess.isEmpty()) {
                    out.println(guess);
                    guessField.setText(""); // Clear guess field after sending
                }
            }
        });
    }

    // Connect to the server and set up streams
    private void connectToServer(String serverIp, int serverPort, String username) {
        try {
            client = new Socket(serverIp, serverPort);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);

            // Send the username to the server
            out.println(username);
            connected = true;

            // Enable guess input and disable connect button
            guessField.setEnabled(true);
            guessButton.setEnabled(true);
            connectButton.setEnabled(false);
            usernameField.setEditable(false);

            // Start a new thread to listen for server messages
            new Thread(this).start();
            messageArea.append("Connected to server as " + username + "\n");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unable to connect to server.");
        }
    }

    // Run method to handle incoming messages from server
    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                messageArea.append(message + "\n");
            }
        } catch (Exception e) {
            messageArea.append("Disconnected from server.\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientGUI clientGUI = new ClientGUI("127.0.0.1", 8080); // Update with server IP if needed
            clientGUI.setVisible(true);
        });
    }
}
