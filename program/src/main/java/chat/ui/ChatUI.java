package main.java.chat.ui;

import main.java.chat.model.Message;
import main.java.chat.network.ChatClient;

import javax.swing.*;
import java.awt.*;

/**
 * Main UI class for the chat application.
 */
public class ChatUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private LoginPanel loginPanel;
    private MainPanel mainPanel;
    private ChatClient client;
    
    public ChatUI() {
        setTitle("SecureChat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 700);
        setLocationRelativeTo(null);
        
        // Initialize components
        initComponents();
        
        // Show the login panel initially
        cardLayout.show(cardPanel, "login");
        
        setVisible(true);
    }
    
    private void initComponents() {
        // Create card layout for switching between panels
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        // Create the client
        client = new ChatClient(this);
        
        // Create login panel
        loginPanel = new LoginPanel(this, client);
        cardPanel.add(loginPanel, "login");
        
        // Create main panel
        mainPanel = new MainPanel(this, client);
        cardPanel.add(mainPanel, "main");
        
        // Add card panel to frame
        add(cardPanel, BorderLayout.CENTER);
        
        // Register shutdown hook to disconnect client on exit
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                client.disconnect();
            }
        });
    }
    
    /**
     * Shows the main chat screen.
     */
    public void showMainScreen() {
        cardLayout.show(cardPanel, "main");
        mainPanel.initialize();
    }
    
    /**
     * Shows the login screen.
     */
    public void showLoginScreen() {
        cardLayout.show(cardPanel, "login");
    }
    
    /**
     * Displays an error message.
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Displays a received message.
     */
    public void displayMessage(Message message) {
        mainPanel.displayMessage(message);
    }
    
    /**
     * Updates a message's status.
     */
    public void updateMessageStatus(String messageId) {
        mainPanel.updateMessageStatus(messageId);
    }
    
    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Start the UI on the EDT
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatUI();
            }
        });
    }
}