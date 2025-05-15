package main.java.chat.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Represents a message in the chat application.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String messageId;
    private String senderId;
    private String recipientId; // Can be a user ID or group ID
    private MessageType type;
    private String content;
    private byte[] mediaContent;
    private String mediaType;
    private Date timestamp;
    private MessageStatus status;
    
    public Message(String senderId, String recipientId, MessageType type, String content) {
        this.messageId = UUID.randomUUID().toString();
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.type = type;
        this.content = content;
        this.timestamp = new Date();
        this.status = MessageStatus.SENT;
    }
    
    public Message(String senderId, String recipientId, MessageType type, 
                  String content, byte[] mediaContent, String mediaType) {
        this(senderId, recipientId, type, content);
        this.mediaContent = mediaContent;
        this.mediaType = mediaType;
    }
    
    // Getters and setters
    public String getMessageId() {
        return messageId;
    }
    
    public String getSenderId() {
        return senderId;
    }
    
    public String getRecipientId() {
        return recipientId;
    }
    
    public MessageType getType() {
        return type;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public byte[] getMediaContent() {
        return mediaContent;
    }
    
    public void setMediaContent(byte[] mediaContent) {
        this.mediaContent = mediaContent;
    }
    
    public String getMediaType() {
        return mediaType;
    }
    
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public MessageStatus getStatus() {
        return status;
    }
    
    public void setStatus(MessageStatus status) {
        this.status = status;
    }
}