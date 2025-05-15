package main.java.chat.ui;

import main.java.chat.model.User;
import main.java.chat.network.ChatClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel for user login and registration.
 */
public class LoginPanel extends JPanel {
    private final ChatUI parent;
    private final ChatClient client;
    
    private JTabbedPane tabbedPane;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField regUsernameField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmPasswordField;
    private JTextField regEmailField;
    
    public LoginPanel(ChatUI parent, ChatClient client) {
        this.parent = parent;
        this.client = client;
        
        setLayout(new BorderLayout());
        initComponents();
    }
    
    private void initComponents() {
        // Create logo panel
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoPanel.setBackground(new Color(3, 94, 3));
        
        // App name
        JLabel titleLabel = new JLabel("SecureChat");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        logoPanel.add(titleLabel);
        
        // Add logo panel to the top
        add(logoPanel, BorderLayout.NORTH);
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Login tab
        JPanel loginPanel = createLoginPanel();
        tabbedPane.addTab("Login", loginPanel);
        
        // Register tab
        JPanel registerPanel = createRegisterPanel();
        tabbedPane.addTab("Register", registerPanel);
        
        // Add tabbed pane to center
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        // Username field
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, usernameField.getPreferredSize().height));
        
        // Password field
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, passwordField.getPreferredSize().height));
        
        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setBackground(new Color(3, 94, 3));
        loginButton.setForeground(Color.WHITE);
        
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginUser();
            }
        });
        
        // Add components
        panel.add(usernameLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(usernameField);
        panel.add(Box.createVerticalStrut(15));
        panel.add(passwordLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(loginButton);
        
        return panel;
    }
    
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        // Username field
        JLabel regUsernameLabel = new JLabel("Username:");
        regUsernameField = new JTextField(20);
        regUsernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, regUsernameField.getPreferredSize().height));
        
        // Email field
        JLabel regEmailLabel = new JLabel("Email:");
        regEmailField = new JTextField(20);
        regEmailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, regEmailField.getPreferredSize().height));
        
        // Password field
        JLabel regPasswordLabel = new JLabel("Password:");
        regPasswordField = new JPasswordField(20);
        regPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, regPasswordField.getPreferredSize().height));
        
        // Confirm password field
        JLabel regConfirmPasswordLabel = new JLabel("Confirm Password:");
        regConfirmPasswordField = new JPasswordField(20);
        regConfirmPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, regConfirmPasswordField.getPreferredSize().height));
        
        // Register button
        JButton registerButton = new JButton("Register");
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setBackground(new Color(3, 94, 3));
        registerButton.setForeground(Color.WHITE);
        
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });
        
        // Add components
        panel.add(regUsernameLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(regUsernameField);
        panel.add(Box.createVerticalStrut(15));
        panel.add(regEmailLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(regEmailField);
        panel.add(Box.createVerticalStrut(15));
        panel.add(regPasswordLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(regPasswordField);
        panel.add(Box.createVerticalStrut(15));
        panel.add(regConfirmPasswordLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(regConfirmPasswordField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(registerButton);
        
        return panel;
    }
    
    private void loginUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (client.authenticate(username, password)) {
            parent.showMainScreen();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void registerUser() {
        String username = regUsernameField.getText();
        String email = regEmailField.getText();
        String password = new String(regPasswordField.getPassword());
        String confirmPassword = new String(regConfirmPasswordField.getPassword());
        
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match", "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // In a real application, we would send this to server for registration
        // For this demo, we'll just show a success message and switch to login
        JOptionPane.showMessageDialog(this, "Registration successful! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
        
        // Clear registration fields
        regUsernameField.setText("");
        regEmailField.setText("");
        regPasswordField.setText("");
        regConfirmPasswordField.setText("");
        
        // Switch to login tab
        tabbedPane.setSelectedIndex(0);
        
        // Pre-fill username
        usernameField.setText(username);
        passwordField.requestFocus();
    }
}