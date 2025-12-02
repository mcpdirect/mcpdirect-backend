package ai.mcpdirect.backend.dao.entity.aitool;

import ai.mcpdirect.backend.util.ID;

public class AIPortTool extends AIPortToolStub{
    public int status;
    public long lastUpdated;
    public int hash;
    public String metaData;
    public String tags;
    public long agentId;
    public int agentStatus;
    public int makerStatus;
    public AIPortTool() {}

    public AIPortTool(long makerId, int status, long lastUpdated, String name, int hash, String metaData, String tags) {
        this.id = ID.nextId();
        this.makerId = makerId;
        this.status = status;
        this.lastUpdated = lastUpdated;
        this.name = name;
        this.hash = hash;
        this.metaData = metaData;
        this.tags = tags;
    }
}