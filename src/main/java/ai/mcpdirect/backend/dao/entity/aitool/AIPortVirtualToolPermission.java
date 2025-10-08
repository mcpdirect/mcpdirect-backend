package ai.mcpdirect.backend.dao.entity.aitool;

public class AIPortVirtualToolPermission {
    public long userId;
    public long accessKeyId;
    public long toolId;
    public long lastUpdated;
    public short status;

    public AIPortVirtualToolPermission() {}

    public AIPortVirtualToolPermission(long userId, long accessKeyId, long toolId, long lastUpdated, short status) {
        this.userId = userId;
        this.accessKeyId = accessKeyId;
        this.toolId = toolId;
        this.lastUpdated = lastUpdated;
        this.status = status;
    }
}
