package entity;

public class AIPortAITool {
    public long id;
    public long makerId;
    public short status;
    public long lastUpdated;
    public String name;
    public String metaData;
    public int hash;
    public String tags;
    public long agentId;
    public short agentStatus;
    public short makerStatus;

    // Constructor
    public AIPortAITool() {}

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMakerId() {
        return makerId;
    }

    public void setMakerId(long makerId) {
        this.makerId = makerId;
    }

    public short getStatus() {
        return status;
    }

    public void setStatus(short status) {
        this.status = status;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public int getHash() {
        return hash;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public long getAgentId() {
        return agentId;
    }

    public void setAgentId(long agentId) {
        this.agentId = agentId;
    }

    public short getAgentStatus() {
        return agentStatus;
    }

    public void setAgentStatus(short agentStatus) {
        this.agentStatus = agentStatus;
    }

    public short getMakerStatus() {
        return makerStatus;
    }

    public void setMakerStatus(short makerStatus) {
        this.makerStatus = makerStatus;
    }
}