package main.java.chat.network;

import main.java.chat.model.Message;
import main.java.chat.model.User;
import main.java.chat.ui.ChatUI;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Client for the chat application that connects to the server and handles message sending/receiving.
 */
public class ChatClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 6001;
    
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private User user;
    private final ChatUI ui;
    private boolean connected = false;
    private Thread listenerThread;
    
    public ChatClient(ChatUI ui) {
        this.ui = ui;
    }
    
    /**
     * Connects to the server.
     */
    public boolean connect() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            connected = true;
            
            // Start listening for messages
            startMessageListener();
            
            return true;
        } catch (IOException e) {
            ui.showError("Connection failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Authenticates with the server using the given credentials.
     */
    public boolean authenticate(String username, String password) {
        if (!connected) {
            if (!connect()) {
                return false;
            }
        }
        
        try {
            // Create a temporary user with credentials
            User tempUser = new User(username, password, "");
            
            // Send to server
            output.writeObject(tempUser);
            
            // Wait for response
            boolean success = (boolean) input.readObject();
            
            if (success) {
                // If successful, server sends back the complete user object
                this.user = (User) input.readObject();
                return true;
            } else {
                disconnect();
                return false;
            }
        } catch (IOException | ClassNotFoundException e) {
            ui.showError("Authentication failed: " + e.getMessage());
            disconnect();
            return false;
        }
    }
    
    /**
     * Starts a thread to listen for incoming messages.
     */
    private void startMessageListener() {
        listenerThread = new Thread(() -> {
            try {
                while (connected) {
                    Object obj = input.readObject();
                    if (obj instanceof Message) {
                        handleMessage((Message) obj);
                    } else if (obj instanceof String) {
                        handleCommand((String) obj);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                if (connected) {
                    ui.showError("Connection lost: " + e.getMessage());
                    disconnect();
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
    
    /**
     * Handles an incoming message.
     */
    private void handleMessage(Message message) {
        ui.displayMessage(message);
    }
    
    /**
     * Handles a command from the server.
     */
    private void handleCommand(String command) {
        if (command.startsWith("DELIVERY_RECEIPT:")) {
            String messageId = command.substring("DELIVERY_RECEIPT:".length());
            ui.updateMessageStatus(messageId);
        }
    }
    
    /**
     * Sends a message to the server.
     */
    public void sendMessage(Message message) {
        if (!connected) {
            ui.showError("Not connected to server");
            return;
        }
        
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            ui.showError("Failed to send message: " + e.getMessage());
        }
    }
    
    /**
     * Disconnects from the server.
     */
    public void disconnect() {
        try {
            connected = false;
            
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            ui.showError("Error disconnecting: " + e.getMessage());
        }
    }
    
    /**
     * Gets the current user.
     */
    public User getUser() {
        return user;
    }
}