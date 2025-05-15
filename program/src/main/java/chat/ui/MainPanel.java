package main.java.chat.ui;

import main.java.chat.model.Message;
import main.java.chat.model.MessageType;
import main.java.chat.model.User;
import main.java.chat.network.ChatClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Main panel for the chat interface after login.
 */
public class MainPanel extends JPanel {
    private final ChatUI parent;
    private final ChatClient client;
    
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel chatPanel;
    private JScrollPane chatScrollPane;
    private JTextField messageField;
    private JButton sendButton;
    private JButton attachButton;
    
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    
    private String currentChatUsername;
    private Map<String, Box> chatHistories;
    
    public MainPanel(ChatUI parent, ChatClient client) {
        this.parent = parent;
        this.client = client;
        
        setLayout(new BorderLayout());
        chatHistories = new HashMap<>();
        
        initComponents();
    }
    
    private void initComponents() {
        // Create left panel (contacts)
        leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        
        // User info panel
        JPanel userInfoPanel = new JPanel(new BorderLayout());
        userInfoPanel.setBackground(new Color(3, 94, 3));
        userInfoPanel.setPreferredSize(new Dimension(0, 60));
        
        JLabel userLabel = new JLabel("Welcome, User");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Arial", Font.BOLD, 16));
        userLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        userInfoPanel.add(userLabel, BorderLayout.CENTER);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.disconnect();
                parent.showLoginScreen();
            }
        });
        userInfoPanel.add(logoutButton, BorderLayout.EAST);
        
        leftPanel.add(userInfoPanel, BorderLayout.NORTH);
        
        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(0, 30));
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        leftPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Contacts list
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setCellRenderer(new ContactListCellRenderer());
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = userList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        String username = userListModel.getElementAt(index);
                        switchToChat(username);
                    }
                }
            }
        });
        
        JScrollPane contactsScrollPane = new JScrollPane(userList);
        contactsScrollPane.setBorder(null);
        leftPanel.add(contactsScrollPane, BorderLayout.CENTER);
        
        // Create right panel (chat)
        rightPanel = new JPanel(new BorderLayout());
        
        // Chat header
        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setBackground(new Color(3, 94, 3));
        chatHeader.setPreferredSize(new Dimension(0, 60));
        
        JLabel chatTitleLabel = new JLabel("Select a contact to start chatting");
        chatTitleLabel.setForeground(Color.WHITE);
        chatTitleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        chatTitleLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        chatHeader.add(chatTitleLabel, BorderLayout.CENTER);
        
        rightPanel.add(chatHeader, BorderLayout.NORTH);
        
        // Chat messages panel
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(new Color(240, 240, 240));
        
        chatScrollPane = new JScrollPane(chatPanel);
        chatScrollPane.setBorder(null);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        rightPanel.add(chatScrollPane, BorderLayout.CENTER);
        
        // Message input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(Color.WHITE);
        
        // Attach button
        attachButton = new JButton("📎");
        attachButton.setPreferredSize(new Dimension(40, 40));
        attachButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attachFile();
            }
        });
        inputPanel.add(attachButton, BorderLayout.WEST);
        
        // Message field
        messageField = new JTextField();
        messageField.setBorder(new EmptyBorder(5, 10, 5, 10));
        inputPanel.add(messageField, BorderLayout.CENTER);
        
        // Send button
        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(3, 94, 3));
        sendButton.setForeground(Color.WHITE);
        sendButton.setPreferredSize(new Dimension(80, 0));
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        rightPanel.add(inputPanel, BorderLayout.SOUTH);
        
        // Add panels to main panel
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }
    
    /**
     * Initializes the panel with user data.
     */
    public void initialize() {
        // Update username in UI
        User user = client.getUser();
        if (user != null) {
            JLabel userLabel = (JLabel) ((JPanel) leftPanel.getComponent(0)).getComponent(0);
            userLabel.setText("Welcome, " + user.getDisplayName());
            
            // Populate user list (in a real app, this would come from the server)
            userListModel.clear();
            userListModel.addElement("Gaitonde");
            userListModel.addElement("Bunty");
            userListModel.addElement("Group Chat");
        }
    }
    
    /**
     * Switches to a chat with the given username.
     */
    private void switchToChat(String username) {
        currentChatUsername = username;
        
        // Update chat header
        JLabel chatTitleLabel = (JLabel) ((JPanel) rightPanel.getComponent(0)).getComponent(0);
        chatTitleLabel.setText(username);
        
        // Load chat history
        Box chatHistory = chatHistories.get(username);
        if (chatHistory == null) {
            chatHistory = Box.createVerticalBox();
            chatHistories.put(username, chatHistory);
        }
        
        // Update chat panel
        chatPanel.removeAll();
        chatPanel.add(chatHistory);
        chatPanel.revalidate();
        chatPanel.repaint();
        
        // Scroll to bottom
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }
    
    /**
     * Sends a message to the current chat.
     */
    private void sendMessage() {
        if (currentChatUsername == null) {
            JOptionPane.showMessageDialog(this, "Please select a contact first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String text = messageField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        
        // Clear message field
        messageField.setText("");
        
        // Create message
        User currentUser = client.getUser();
        Message message = new Message(currentUser.getUserId(), "recipient-id", MessageType.TEXT, text);
        
        // Display the message
        displaySentMessage(message);
        
        // In a real app, send the message through the client
        // client.sendMessage(message);
    }
    
    /**
     * Opens a file chooser to attach a file.
     */
    private void attachFile() {
        if (currentChatUsername == null) {
            JOptionPane.showMessageDialog(this, "Please select a contact first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select File to Attach");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                // For images, resize and convert to byte array
                if (isImageFile(selectedFile)) {
                    // Read and scale the image
                    Image image = ImageIO.read(selectedFile);
                    int maxSize = 300;
                    int width = image.getWidth(null);
                    int height = image.getHeight(null);
                    
                    if (width > maxSize || height > maxSize) {
                        double scale = Math.min((double) maxSize / width, (double) maxSize / height);
                        width = (int) (width * scale);
                        height = (int) (height * scale);
                    }
                    
                    Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    
                    // Convert to byte array
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(toBufferedImage(scaledImage), "png", baos);
                    byte[] imageData = baos.toByteArray();
                    
                    // Create message
                    User currentUser = client.getUser();
                    Message message = new Message(
                            currentUser.getUserId(), "recipient-id", MessageType.IMAGE,
                            "Image: " + selectedFile.getName(), imageData, "image/png");
                    
                    // Display the message
                    displaySentMessage(message);
                    
                    // In a real app, send the message through the client
                    // client.sendMessage(message);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage(), 
                        "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Converts an Image to a BufferedImage.
     */
    private java.awt.image.BufferedImage toBufferedImage(Image image) {
        java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(
                image.getWidth(null), image.getHeight(null), java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics g = bufferedImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bufferedImage;
    }
    
    /**
     * Checks if a file is an image.
     */
    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || 
               name.endsWith(".png") || name.endsWith(".gif");
    }
    
    /**
     * Displays a sent message in the chat.
     */
    private void displaySentMessage(Message message) {
        if (currentChatUsername == null) {
            return;
        }
        
        Box chatHistory = chatHistories.get(currentChatUsername);
        
        // Create message panel
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setOpaque(false);
        messagePanel.setBorder(new EmptyBorder(5, 50, 5, 10));
        
        // Text or media content
        if (message.getType() == MessageType.TEXT) {
            // Text message
            JLabel messageLabel = new JLabel(message.getContent());
            messageLabel.setOpaque(true);
            messageLabel.setBackground(new Color(37, 211, 102));
            messageLabel.setBorder(new EmptyBorder(8, 15, 8, 15));
            messagePanel.add(messageLabel, BorderLayout.EAST);
        } else if (message.getType() == MessageType.IMAGE) {
            // Image message
            JPanel mediaPanel = new JPanel(new BorderLayout());
            mediaPanel.setOpaque(true);
            mediaPanel.setBackground(new Color(37, 211, 102));
            mediaPanel.setBorder(new EmptyBorder(8, 15, 8, 15));
            
            // Create image icon from byte array
            byte[] mediaContent = message.getMediaContent();
            if (mediaContent != null) {
                ImageIcon icon = new ImageIcon(mediaContent);
                JLabel imageLabel = new JLabel(icon);
                mediaPanel.add(imageLabel, BorderLayout.CENTER);
            }
            
            // Add caption if any
            if (message.getContent() != null && !message.getContent().isEmpty()) {
                JLabel captionLabel = new JLabel(message.getContent());
                captionLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
                mediaPanel.add(captionLabel, BorderLayout.SOUTH);
            }
            
            messagePanel.add(mediaPanel, BorderLayout.EAST);
        }
        
        // Add timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        JLabel timeLabel = new JLabel(sdf.format(message.getTimestamp()));
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        timeLabel.setForeground(Color.GRAY);
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        timeLabel.setBorder(new EmptyBorder(2, 0, 0, 0));
        
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(messagePanel, BorderLayout.CENTER);
        wrapperPanel.add(timeLabel, BorderLayout.SOUTH);
        
        chatHistory.add(wrapperPanel);
        chatHistory.add(Box.createVerticalStrut(10));
        
        // Update UI
        chatPanel.revalidate();
        chatPanel.repaint();
        
        // Scroll to bottom
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }
    
    /**
     * Displays a received message in the chat.
     */
    public void displayMessage(Message message) {
        // In a real app, determine which chat this message belongs to
        // and update the appropriate chat history
    }
    
    /**
     * Updates a message's status.
     */
    public void updateMessageStatus(String messageId) {
        // In a real app, find the message with this ID and update its status
    }
    
    /**
     * Custom cell renderer for the contacts list.
     */
    private class ContactListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            // Set icon (would be user avatar in a real app)
            // label.setIcon(...);
            
            label.setBorder(new EmptyBorder(10, 10, 10, 10));
            return label;
        }
    }
}