package ai.mcpdirect.backend.dao.entity.account;

public class AIPortAnonymousCredential extends AIPortAnonymous{
    public String secretKey;
    public AIPortAnonymousCredential(){}

    public AIPortAnonymousCredential(long id, String deviceId,String secretKey,int status,String keySeed) {
        super(id,deviceId,status,keySeed);
        this.secretKey = secretKey;
    }
}
