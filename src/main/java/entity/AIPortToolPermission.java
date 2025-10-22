package entity;

public class AIPortToolPermission {
    public long userId;
    public long accessKeyId;
    public long toolId;
    public long lastUpdated;
    public short status;

    // Constructor
    public AIPortToolPermission() {}

    // Getters and setters
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(long accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public long getToolId() {
        return toolId;
    }

    public void setToolId(long toolId) {
        this.toolId = toolId;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public short getStatus() {
        return status;
    }

    public void setStatus(short status) {
        this.status = status;
    }
}