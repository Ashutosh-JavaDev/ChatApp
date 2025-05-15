package main.java.chat.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a user in the chat application.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String userId;
    private String username;
    private String password; // Stored as hash in a real application
    private String email;
    private String displayName;
    private byte[] profileImage;
    private UserStatus status;
    private long lastSeen;
    
    public User(String username, String password, String email) {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
        this.password = password;
        this.email = email;
        this.displayName = username;
        this.status = UserStatus.OFFLINE;
        this.lastSeen = System.currentTimeMillis();
    }
    
    // Getters and setters
    public String getUserId() {
        return userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public byte[] getProfileImage() {
        return profileImage;
    }
    
    public void setProfileImage(byte[] profileImage) {
        this.profileImage = profileImage;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public void setStatus(UserStatus status) {
        this.status = status;
        if (status != UserStatus.OFFLINE) {
            this.lastSeen = System.currentTimeMillis();
        }
    }
    
    public long getLastSeen() {
        return lastSeen;
    }
    
    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return userId.equals(user.userId);
    }
    
    @Override
    public int hashCode() {
        return userId.hashCode();
    }
}