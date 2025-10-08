package ai.mcpdirect.backend.dao.entity.account;

public class AIPortTeamMember {
    public long teamId;
    public long memberId;
    public Integer status;
    public long created;
    public Long expirationDate;

    public AIPortTeamMember() {}

    public AIPortTeamMember(long teamId, long memberId, int status, long created, long expirationDate) {
        this.teamId = teamId;
        this.memberId = memberId;
        this.status = status;
        this.created = created;
        this.expirationDate = expirationDate;
    }
}