package ai.mcpdirect.backend.dao.entity.aitool;

public class AIPortToolMakerTeam {
    public long toolMakerId;
    public long teamId;
    public short status;
    public long created;

    public AIPortToolMakerTeam() {}

    public AIPortToolMakerTeam(long toolMakerId, long teamId, short status, long created) {
        this.toolMakerId = toolMakerId;
        this.teamId = teamId;
        this.status = status;
        this.created = created;
    }
}
