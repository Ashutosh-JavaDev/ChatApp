package main.java.chat.network;

import main.java.chat.model.Message;
import main.java.chat.model.MessageStatus;
import main.java.chat.model.User;
import main.java.chat.model.UserStatus;
import main.java.chat.persistence.DatabaseManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server for the chat application that handles client connections and message routing.
 */
public class ChatServer {
    private static final int PORT = 6001;
    private static final int MAX_CLIENTS = 100;
    
    private final Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
    private final DatabaseManager dbManager = new DatabaseManager();
    private ServerSocket serverSocket;
    
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Chat server started on port " + PORT);
            
            // Accept client connections
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                
                // Create a new handler for this client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            shutdown();
        }
    }
    
    public void shutdown() {
        try {
            // Close all client connections
            for (ClientHandler handler : connectedClients.values()) {
                handler.close();
            }
            connectedClients.clear();
            
            // Close the server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            // Shutdown the thread pool
            threadPool.shutdown();
            
            System.out.println("Server shutdown complete");
        } catch (IOException e) {
            System.err.println("Error during server shutdown: " + e.getMessage());
        }
    }
    
    /**
     * Broadcasts a message to all connected clients.
     */
    private void broadcastMessage(Message message) {
        for (ClientHandler handler : connectedClients.values()) {
            handler.sendMessage(message);
        }
    }
    
    /**
     * Sends a message to a specific user.
     */
    private void sendDirectMessage(Message message) {
        String recipientId = message.getRecipientId();
        ClientHandler recipient = connectedClients.get(recipientId);
        
        if (recipient != null) {
            // Recipient is online, send the message directly
            recipient.sendMessage(message);
            
            // Update message status to delivered
            message.setStatus(MessageStatus.DELIVERED);
            
            // Notify the sender about the delivery
            ClientHandler sender = connectedClients.get(message.getSenderId());
            if (sender != null) {
                sender.sendDeliveryReceipt(message);
            }
        }
        
        // Store the message in the database regardless of delivery status
        dbManager.saveMessage(message);
    }
    
    /**
     * Handles communication with a single client.
     */
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        private User user;
        private boolean running = true;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                this.output = new ObjectOutputStream(socket.getOutputStream());
                this.input = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                System.err.println("Error creating streams: " + e.getMessage());
                close();
            }
        }
        
        @Override
        public void run() {
            try {
                // Perform authentication
                authenticate();
                
                // Handle messages
                while (running) {
                    Object obj = input.readObject();
                    if (obj instanceof Message) {
                        handleMessage((Message) obj);
                    }
                }
            } catch (EOFException e) {
                // Client disconnected
                System.out.println("Client disconnected: " + (user != null ? user.getUsername() : "unknown"));
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                close();
            }
        }
        
        /**
         * Authenticates the client.
         */
        private void authenticate() throws IOException, ClassNotFoundException {
            Object obj = input.readObject();
            if (obj instanceof User) {
                User clientUser = (User) obj;
                
                // In a real application, verify credentials against database
                User authenticatedUser = dbManager.authenticateUser(
                        clientUser.getUsername(), clientUser.getPassword());
                
                if (authenticatedUser != null) {
                    this.user = authenticatedUser;
                    this.user.setStatus(UserStatus.ONLINE);
                    
                    // Add to connected clients
                    connectedClients.put(user.getUserId(), this);
                    
                    // Send success response
                    output.writeObject(true);
                    output.writeObject(user);
                    
                    // Send offline messages
                    sendOfflineMessages();
                    
                    // Notify other users about status change
                    broadcastStatusChange();
                    
                    System.out.println("User authenticated: " + user.getUsername());
                } else {
                    // Authentication failed
                    output.writeObject(false);
                    throw new IOException("Authentication failed");
                }
            } else {
                output.writeObject(false);
                throw new IOException("Invalid authentication request");
            }
        }
        
        /**
         * Handles an incoming message.
         */
        private void handleMessage(Message message) {
            // Save to database and route to recipient(s)
            sendDirectMessage(message);
        }
        
        /**
         * Sends a message to this client.
         */
        public void sendMessage(Message message) {
            try {
                output.writeObject(message);
                output.flush();
            } catch (IOException e) {
                System.err.println("Error sending message: " + e.getMessage());
            }
        }
        
        /**
         * Sends a delivery receipt to this client.
         */
        public void sendDeliveryReceipt(Message message) {
            try {
                output.writeObject("DELIVERY_RECEIPT:" + message.getMessageId());
                output.flush();
            } catch (IOException e) {
                System.err.println("Error sending delivery receipt: " + e.getMessage());
            }
        }
        
        /**
         * Sends all offline messages to this client.
         */
        private void sendOfflineMessages() {
            for (Message message : dbManager.getOfflineMessages(user.getUserId())) {
                sendMessage(message);
                message.setStatus(MessageStatus.DELIVERED);
                dbManager.updateMessageStatus(message);
            }
        }
        
        /**
         * Broadcasts the user's status change to other users.
         */
        private void broadcastStatusChange() {
            // Implementation depends on how you want to handle status updates
        }
        
        /**
         * Closes the connection to this client.
         */
        public void close() {
            try {
                running = false;
                
                if (user != null) {
                    // Update user status
                    user.setStatus(UserStatus.OFFLINE);
                    user.setLastSeen(System.currentTimeMillis());
                    dbManager.updateUser(user);
                    
                    // Remove from connected clients
                    connectedClients.remove(user.getUserId());
                    
                    // Broadcast status change
                    broadcastStatusChange();
                }
                
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing client connection: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}