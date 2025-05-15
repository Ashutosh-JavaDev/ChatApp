package main.java.chat;

import main.java.chat.ui.ChatUI;

import javax.swing.*;

/**
 * Main entry point for the chat application.
 */
public class Main {
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