package main.java.chat.persistence;

import main.java.chat.model.Message;
import main.java.chat.model.MessageType;
import main.java.chat.model.User;
import main.java.chat.security.PasswordHasher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/chat_app";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "@Radhakrishna297"; // Change this

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public DatabaseManager() {
        try {
            // Initialize database and tables
            createTables();
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                user_id VARCHAR(36) PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                display_name VARCHAR(100),
                status VARCHAR(20) DEFAULT 'OFFLINE',
                last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createMessagesTable = """
            CREATE TABLE IF NOT EXISTS messages (
                message_id VARCHAR(36) PRIMARY KEY,
                sender_id VARCHAR(36) NOT NULL,
                recipient_id VARCHAR(36) NOT NULL,
                message_type VARCHAR(20) NOT NULL,
                content TEXT,
                media_content LONGBLOB,
                media_type VARCHAR(50),
                status VARCHAR(20) DEFAULT 'SENT',
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (sender_id) REFERENCES users(user_id),
                FOREIGN KEY (recipient_id) REFERENCES users(user_id)
            )
        """;

        String createGroupsTable = """
            CREATE TABLE IF NOT EXISTS groups (
                group_id VARCHAR(36) PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                description TEXT,
                group_image LONGBLOB,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createGroupMembersTable = """
            CREATE TABLE IF NOT EXISTS group_members (
                group_id VARCHAR(36),
                user_id VARCHAR(36),
                is_admin BOOLEAN DEFAULT FALSE,
                joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (group_id, user_id),
                FOREIGN KEY (group_id) REFERENCES groups(group_id),
                FOREIGN KEY (user_id) REFERENCES users(user_id)
            )
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createMessagesTable);
            stmt.execute(createGroupsTable);
            stmt.execute(createGroupMembersTable);
        }
    }

    public User authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (PasswordHasher.verifyPassword(password, storedPassword)) {
                    return new User(
                        rs.getString("username"),
                        storedPassword,
                        rs.getString("email")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }
        return null;
    }

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (user_id, username, password, email, display_name) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String hashedPassword = PasswordHasher.hashPassword(user.getPassword());
            
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, hashedPassword);
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getDisplayName());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    public void updateUser(User user) {
        String sql = "UPDATE users SET status = ?, last_seen = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getStatus().toString());
            pstmt.setString(2, user.getUserId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
    }

    public void saveMessage(Message message) {
        String sql = """
            INSERT INTO messages (message_id, sender_id, recipient_id, message_type, 
                                content, media_content, media_type, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, message.getMessageId());
            pstmt.setString(2, message.getSenderId());
            pstmt.setString(3, message.getRecipientId());
            pstmt.setString(4, message.getType().toString());
            pstmt.setString(5, message.getContent());
            pstmt.setBytes(6, message.getMediaContent());
            pstmt.setString(7, message.getMediaType());
            pstmt.setString(8, message.getStatus().toString());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving message: " + e.getMessage());
        }
    }

    public List<Message> getMessagesBetween(String user1Id, String user2Id) {
        String sql = """
            SELECT * FROM messages 
            WHERE (sender_id = ? AND recipient_id = ?) 
               OR (sender_id = ? AND recipient_id = ?)
            ORDER BY timestamp ASC
        """;
        
        List<Message> messages = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user1Id);
            pstmt.setString(2, user2Id);
            pstmt.setString(3, user2Id);
            pstmt.setString(4, user1Id);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Message message = new Message(
                    rs.getString("sender_id"),
                    rs.getString("recipient_id"),
                    MessageType.valueOf(rs.getString("message_type")),
                    rs.getString("content"),
                    rs.getBytes("media_content"),
                    rs.getString("media_type")
                );
                messages.add(message);
            }
        } catch (SQLException e) {
            System.err.println("Error getting messages: " + e.getMessage());
        }
        
        return messages;
    }

    public List<Message> getOfflineMessages(String userId) {
        String sql = "SELECT * FROM messages WHERE recipient_id = ? AND status = 'SENT' ORDER BY timestamp ASC";
        List<Message> messages = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Message message = new Message(
                    rs.getString("sender_id"),
                    rs.getString("recipient_id"),
                    MessageType.valueOf(rs.getString("message_type")),
                    rs.getString("content"),
                    rs.getBytes("media_content"),
                    rs.getString("media_type")
                );
                messages.add(message);
            }
        } catch (SQLException e) {
            System.err.println("Error getting offline messages: " + e.getMessage());
        }
        
        return messages;
    }

    public void updateMessageStatus(Message message) {
        String sql = "UPDATE messages SET status = ? WHERE message_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, message.getStatus().toString());
            pstmt.setString(2, message.getMessageId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating message status: " + e.getMessage());
        }
    }
}