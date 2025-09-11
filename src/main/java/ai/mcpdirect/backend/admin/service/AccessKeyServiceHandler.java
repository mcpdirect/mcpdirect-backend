package ai.mcpdirect.backend.admin.service;

import java.util.Objects;

import appnet.hstp.SimpleServiceResponseMessage;
import appnet.hstp.annotation.ServiceName;
import appnet.hstp.annotation.ServiceRequestInit;
import appnet.hstp.annotation.ServiceRequestMapping;
import appnet.hstp.annotation.ServiceRequestMessage;
import appnet.hstp.annotation.ServiceResponseMessage;
import ai.mcpdirect.backend.util.AIPortAccessKeyGenerator;
import ai.mcpdirect.backend.util.AIPortAccessKeyValidator;
import ai.mcpdirect.backend.util.KeyValueCache;
import ai.mcpdirect.backend.util.KeyValueCacheFactory;

@ServiceName("access_key")
@ServiceRequestMapping("/")
public class AccessKeyServiceHandler {

//    private KeyValueCache cache;
//    @ServiceRequestInit
//    public void init(){
//        cache = KeyValueCacheFactory.getInstance();
//    }
    public static class RequestOfVerify extends RequestOfCreate{
        public String accessKey;
        public RequestOfVerify (){}

        public RequestOfVerify (long userId,int userDevice,String accessKey){
            super(userId, userDevice);
            this.accessKey = accessKey;
        }
    }
    @ServiceRequestMapping("verify")
    public void verify(
        @ServiceRequestMessage RequestOfVerify req,
        @ServiceResponseMessage SimpleServiceResponseMessage<Boolean> resp
    ){
        if(req.userId>0&&req.accessKey!=null){
            resp.success(Objects.equals(KeyValueCacheFactory.getInstance().get(req.userId+"-"+req.userDevice), req.accessKey));
        }else{
            resp.success(false);
        }
    }

    public static class RequestOfCreate {
        public long userId;
        public int userDevice;
        public RequestOfCreate (){}
        public RequestOfCreate (long userId,int userDevice){
            this.userId = userId;
            this.userDevice = userDevice;
        }
    }
    @ServiceRequestMapping("create")
    public void create(
        @ServiceRequestMessage RequestOfCreate req,
        @ServiceResponseMessage SimpleServiceResponseMessage<String> resp
    ) throws Exception{
        if(req.userId>0){
            String apiKey = AIPortAccessKeyGenerator.generateApiKey(
                            AIPortAccessKeyValidator.PREFIX_AUK, req.userId);
            KeyValueCacheFactory.getInstance().set(req.userId+"-"+req.userDevice, apiKey, 3600*24*30);
            resp.success(apiKey);
        }else{
            resp.success(null);
        }
    }

    @ServiceRequestMapping("remove")
    public void remove(
            @ServiceRequestMessage RequestOfVerify req,
            @ServiceResponseMessage SimpleServiceResponseMessage<Boolean> resp
    ){
        if(req.userId>0&&req.accessKey!=null){
            KeyValueCacheFactory.getInstance().remove(req.userId+"-"+req.userDevice);
            resp.success(true);
        }else{
            resp.success(false);
        }
    }
}
