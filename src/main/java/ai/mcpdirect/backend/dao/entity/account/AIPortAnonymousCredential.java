package ai.mcpdirect.backend.dao.entity.account;

public class AIPortAnonymousCredential extends AIPortAnonymous{
    public String secretKey;
    public AIPortAnonymousCredential(){}

    public AIPortAnonymousCredential(long id, String deviceId,String secretKey,int status) {
        super(id,deviceId,status);
        this.secretKey = secretKey;
    }
}
