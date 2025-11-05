package ai.mcpdirect.backend.dao.entity.aitool;

public class AIPortToolMaker {
    public long id;
    public long created;
    public int status;
    public long lastUpdated;
//    public long hash;
//    public String tools;

    public static final int TYPE_VIRTUAL = 0;
    public static final int TYPE_MCP = 1000;
    /**
     * 1000 is MCP
     */
    public int type;
    public String name;
    public String tags;
    public long agentId;
    public int agentStatus;
    public String agentName;
    public long userId;
    public long teamId;
    public long templateId;

    public AIPortToolMaker name(String name){
        if(name==null||(name=name.trim()).isEmpty()||name.length()>50){
            throw new RuntimeException("invalid name");
        }
        this.name = name;
        return this;
    }
    public AIPortToolMaker tags(String tags){
        if(tags!=null&&(tags=tags.trim()).length()>100){
            throw new RuntimeException("invalid tags");
        }
        this.tags = tags;
        return this;
    }
}
