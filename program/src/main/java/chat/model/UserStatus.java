package main.java.chat.model;

/**
 * Enum representing the possible user statuses in the chat application.
 */
public enum UserStatus {
    ONLINE("Online"),
    AWAY("Away"),
    BUSY("Busy"),
    OFFLINE("Offline");
    
    private final String displayText;
    
    UserStatus(String displayText) {
        this.displayText = displayText;
    }
    
    public String getDisplayText() {
        return displayText;
    }
    
    @Override
    public String toString() {
        return displayText;
    }
}