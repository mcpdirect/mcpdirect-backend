package ai.mcpdirect.backend.dao.entity.account;

public class AIPortAnonymous extends AIPortAccount{
//    public long id;
    public String deviceId;
    public AIPortAnonymous(){}

    public AIPortAnonymous(long id){
        super(id);
    }
    public AIPortAnonymous(long id, String deviceId,int status,String keySeed) {
        super(id,"anonymous",status,keySeed);
        this.deviceId = deviceId;
    }
}
