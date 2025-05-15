package main.java.chat.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Represents a chat group in the chat application.
 */
public class Group implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String groupId;
    private String name;
    private String description;
    private byte[] groupImage;
    private List<String> memberIds;
    private List<String> adminIds;
    private Date createdAt;
    
    public Group(String name, String description, String creatorId) {
        this.groupId = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.memberIds = new ArrayList<>();
        this.adminIds = new ArrayList<>();
        this.memberIds.add(creatorId);
        this.adminIds.add(creatorId);
        this.createdAt = new Date();
    }
    
    // Getters and setters
    public String getGroupId() {
        return groupId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public byte[] getGroupImage() {
        return groupImage;
    }
    
    public void setGroupImage(byte[] groupImage) {
        this.groupImage = groupImage;
    }
    
    public List<String> getMemberIds() {
        return new ArrayList<>(memberIds);
    }
    
    public void addMember(String userId) {
        if (!memberIds.contains(userId)) {
            memberIds.add(userId);
        }
    }
    
    public void removeMember(String userId) {
        memberIds.remove(userId);
        adminIds.remove(userId);
    }
    
    public List<String> getAdminIds() {
        return new ArrayList<>(adminIds);
    }
    
    public void addAdmin(String userId) {
        if (memberIds.contains(userId) && !adminIds.contains(userId)) {
            adminIds.add(userId);
        }
    }
    
    public void removeAdmin(String userId) {
        adminIds.remove(userId);
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public boolean isMember(String userId) {
        return memberIds.contains(userId);
    }
    
    public boolean isAdmin(String userId) {
        return adminIds.contains(userId);
    }
}