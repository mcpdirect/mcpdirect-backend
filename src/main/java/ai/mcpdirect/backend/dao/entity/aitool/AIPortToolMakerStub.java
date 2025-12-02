package ai.mcpdirect.backend.dao.entity.aitool;

public class AIPortToolMakerStub {
    public long id;
    public long created;
    public long removed;

    public static final int TYPE_VIRTUAL = 0;
    public static final int TYPE_MCP = 1000;
    /**
     * 1000 is MCP
     */
    public int type;
    public String name;
    public long agentId;
    public long userId;
}
