package ai.mcpdirect.backend.dao.entity.account;

public class AIPortTeam {
    public long id;
    public String name;
    public long created;
    public long ownerId;
    public Integer status;

    public AIPortTeam() {}

    public AIPortTeam(long id, String name, long created, long ownerId,int status) {
        this.id = id;
        this.name = name;
        this.created = created;
        this.ownerId = ownerId;
        this.status = status;
    }
}