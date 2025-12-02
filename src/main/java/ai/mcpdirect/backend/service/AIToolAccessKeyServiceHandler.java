package ai.mcpdirect.backend.service;

import ai.mcpdirect.backend.dao.AIToolDataHelper;
import ai.mcpdirect.backend.dao.AccountDataHelper;
import ai.mcpdirect.backend.dao.entity.account.*;
import ai.mcpdirect.backend.dao.entity.aitool.AIPortToolAccessKey;
import ai.mcpdirect.backend.dao.entity.aitool.AIPortToolAccessKeyCredential;
import ai.mcpdirect.backend.dao.entity.aitool.AIPortToolAgent;
import ai.mcpdirect.backend.dao.mapper.account.AccountMapper;
import ai.mcpdirect.backend.dao.mapper.aitool.AIToolMapper;
import ai.mcpdirect.backend.util.AIPortAccessKeyGenerator;
import ai.mcpdirect.backend.util.AIPortAccessKeyValidator;
import ai.mcpdirect.backend.util.ID;
import appnet.hstp.ServiceEngine;
import appnet.hstp.ServiceRequest;
import appnet.hstp.SimpleServiceResponseMessage;
import appnet.hstp.USL;
import appnet.hstp.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ai.mcpdirect.backend.service.AIPortServiceResponse.ACCOUNT_NOT_EXIST;
import static ai.mcpdirect.backend.service.AIPortServiceResponse.TEAM_NOT_EXIST;
import static ai.mcpdirect.backend.service.AuthenticationServiceHandler.checkHash;

@ServiceName("aitools.management")
@ServiceRequestMapping("/tool_access_key/")
public class AIToolAccessKeyServiceHandler extends ServiceRequestAuthenticationHandler{
    private AIToolDataHelper helper;
    private AIToolMapper toolMapper;
    private AccountMapper accountMapper;
    @ServiceRequestInit
    public void init(ServiceEngine engine) {
        this.engine = engine;
        helper = AIToolDataHelper.getInstance();
        toolMapper = helper.getAIToolMapper();
        accountMapper = AccountDataHelper.getInstance().getAccountMapper();
    }

    @ServiceRequestMapping("create")
    public void createAccessKey(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage AccountServiceHandler.RequestOfCreateAccessKey req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortToolAccessKeyCredential> resp
    ) throws Exception {
        account = accountMapper.selectUserAccountById(account.id);
        if (account != null) {
            long now = System.currentTimeMillis();
//            String secretKey = AIPortAccessKeyGenerator.generateApiKey(
//                    AIPortAccessKeyValidator.PREFIX_AIK, account.id);
            String secretKey = AIPortAccessKeyGenerator.generateApiKey(
                    AIPortAccessKeyValidator.PREFIX_AIK, 0);
            AIPortToolAccessKeyCredential key = new AIPortToolAccessKeyCredential(
                    AIPortAccessKeyValidator.hashCode(secretKey),
                    account.id, req.name,
                    secretKey,
                    1, now, Long.MAX_VALUE,
                    now);
            toolMapper.insertAccessKeyCredential(key);
//            key.secretKey = secretKey;
            resp.success(key);
        }
    }

    public static class RequestOfModifyAccessKey extends AccountServiceHandler.RequestOfCreateAccessKey {
        public long id;
        public Integer status;
    }

    @ServiceRequestMapping("modify")
    public void modifyAccessKey(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfModifyAccessKey req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortToolAccessKey> resp) throws Exception {
        account = accountMapper.selectUserAccountById(account.id);
        AIPortToolAccessKey accessKey;
        if (account != null && (accessKey = toolMapper.selectAccessKeyById(account.id,req.id))!=null) {
            int oldStatus = accessKey.status;
            if(req.status!=null&&req.status<0){
                accessKey.status = -1;
                toolMapper.deleteAccessKey(account.id,accessKey.id);
            }else {
                req.name = (req.name!=null&&!(req.name= req.name.trim()).isEmpty())?req.name:null;
                if(req.name!=null&&req.status!=null) {
                    accessKey.name = req.name;
                    accessKey.status = req.status;
                    toolMapper.updateAccessKey(accessKey);
                }else if(req.name!=null){
                    accessKey.name = req.name;
                    toolMapper.updateAccessKeyName(accessKey);
                }else if(req.status!=null){
                    accessKey.status = req.status;
                    toolMapper.updateAccessKeyStatus(accessKey);
                }
            }
            if(oldStatus!= accessKey.status)
                engine.broadcast(
                        USL.create("aitools@mcpdirect.ai/access_key/update"),
                        "[{\"id\":"+accessKey.id+",\"userId\":"+accessKey.userId+",\"status\":"+accessKey.status+"}]");
            resp.success(accessKey);
        }
    }

    public static class RequestOfQueryAccessKey {
        public Long keyId;
    }

    @ServiceRequestMapping("query")
    public void queryAccessKey(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfQueryAccessKey req,
            @ServiceResponseMessage SimpleServiceResponseMessage<List<AIPortToolAccessKey>> resp){
        if(req.keyId==null||req.keyId==0) {
            resp.success(toolMapper.selectAccessKeyByUserId(account.id));
        }else{
            AIPortToolAccessKey accessKey = toolMapper.selectAccessKeyById(account.id, req.keyId);
            resp.success(accessKey==null?List.of():List.of(accessKey));
        }
    }

    @ServiceRequestMapping("credential/get")
    public void getAccessKeyCredential(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfQueryAccessKey req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortToolAccessKey> resp){
        if(req.keyId!=null&&req.keyId>0) {
            resp.success(toolMapper.selectAccessKeyCredentialById(req.keyId));
        }
    }
}
