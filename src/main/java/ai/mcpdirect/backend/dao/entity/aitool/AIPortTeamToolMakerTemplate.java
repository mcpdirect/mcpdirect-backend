package ai.mcpdirect.backend.dao.entity.aitool;

public class AIPortTeamToolMakerTemplate {
    public long templateId;
    public long teamId;
    public Integer status;
    public long created;
    public long lastUpdated;

    public AIPortTeamToolMakerTemplate templateId(long templateId) {
        this.templateId = templateId;
        return this;
    }

    public AIPortTeamToolMakerTemplate teamId(long teamId) {
        this.teamId = teamId;
        return this;
    }

    public AIPortTeamToolMakerTemplate status(Integer status) {
        this.status = status;
        return this;
    }

    public AIPortTeamToolMakerTemplate created(long created) {
        this.created = created;
        return this;
    }

    public AIPortTeamToolMakerTemplate lastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }
    public static AIPortTeamToolMakerTemplate build(){
        return new AIPortTeamToolMakerTemplate();
    }
}
