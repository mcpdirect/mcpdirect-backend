package ai.mcpdirect.backend.admin.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ai.mcpdirect.backend.dao.entity.account.*;
import appnet.hstp.*;
import appnet.hstp.annotation.*;

import ai.mcpdirect.backend.dao.AIToolDataHelper;
import ai.mcpdirect.backend.dao.AccountDataHelper;
import ai.mcpdirect.backend.dao.entity.aitool.AIPortToolAgent;
import ai.mcpdirect.backend.dao.mapper.account.AccountMapper;
import ai.mcpdirect.backend.dao.mapper.aitool.AIToolMapper;
import ai.mcpdirect.backend.util.AIPortAccessKeyGenerator;
import ai.mcpdirect.backend.util.AIPortAccessKeyValidator;

import static ai.mcpdirect.backend.admin.service.AuthenticationServiceHandler.checkHash;

@ServiceName("account.management")
@ServiceRequestMapping("/")
public class AccountServiceHandler extends ServiceRequestAuthenticationHandler{
    private AccountDataHelper helper;
    private AccountMapper accountMapper;
//    private ServiceEngine engine;

    @ServiceRequestInit
    public void init(ServiceEngine engine) {
        this.engine = engine;
        helper = AccountDataHelper.getInstance();
        accountMapper = helper.getAccountMapper();
    }

//    @ServiceRequestAuthentication
//    public AIPortAccount authenticate(
//            ServiceRequest request, Class<?> authObjectType, int[] authRoles, boolean anonymous) {
//        String hstpAuth = request.getRequestHeaders().getHeader("hstp-auth");
//
//        Long userId = AIPortAccessKeyValidator.extractUserId(AIPortAccessKeyValidator.PREFIX_AUK, hstpAuth);
//        if (userId != null) {
//            AccessKeyServiceHandler a = engine.getServiceRequestHandler(AccessKeyServiceHandler.class);
//
//            SimpleServiceResponseMessage<Boolean> ar = new SimpleServiceResponseMessage<>();
//            a.verify(new RequestOfVerify(userId, 0, hstpAuth), ar);
//            if (Boolean.TRUE.equals(ar.data)) {
//                return new AIPortAccount(userId);
//            }
//        }
//        return null;
//    }

    public static class RequestOfCreateAccount {
        public String username;
        public String account;
        public String password;
        public String language;
    }

    @ServiceRequestMapping("account/create")
    public void createAccount(
            @ServiceRequestMessage RequestOfCreateAccount req,
            @ServiceResponseMessage SimpleServiceResponseMessage<Boolean> resp) throws Exception {
        resp.success(false);
        // String username = req.username;
        // String account = req.getString("account","").trim();
        // String password = req.getString("password","").trim();
        // String nickname = req.getString("nickname","").trim();
        // if(account.isEmpty()){
        // account = username;
        // }
        if (req.account.isEmpty()) {
            resp.message = "invalid user name and account";
            return;
        }

        if (accountMapper.checkUserAccount(req.account)>0) {
            resp.message = "user name or account already exists";
            return;
        }
        AIPortAccountCredential credential = new AIPortAccountCredential(
                req.account, 1, req.password,AIPortAccessKeyGenerator.generateRandomKey(16));
        // if(username.isEmpty()){
        // credential.username = "U_"+credential.id;
        // }
        AIPortUser user = new AIPortUser(credential.id, req.username, req.language, System.currentTimeMillis(), 0);

        boolean r = helper.executeSql(sqlSession -> {
            AccountMapper mapper = sqlSession.getMapper(AccountMapper.class);
            mapper.insertUserAccountCredential(credential);
            mapper.insertUser(user);
            return true;
        });
        resp.success(r);
    }

    public static class RequestOfServiceAccount {
        public String account;
    }

    public static class UserInfo {
        public AIPortUser user;
        public AIPortAccount account;

        public UserInfo() {
        }

        public UserInfo(AIPortUser user, AIPortAccount account) {
            this.user = user;
            this.account = account;
        }
    }

    @ServiceRequestMapping("account/search")
    public void searchAccount(
            @ServiceRequestMessage RequestOfServiceAccount req,
            @ServiceResponseMessage SimpleServiceResponseMessage<UserInfo> resp) {
        if (req.account==null||(req.account=req.account.trim()).isEmpty()) {
            resp.message = "invalid user name and account";
            return;
        }
        AIPortAccount account = accountMapper.selectUserAccount(req.account);
        AIPortUser user = null;
        if (account != null) {
            user = accountMapper.selectUser(account.id);
        }
        resp.success(new UserInfo(user, account));
    }
    @ServiceRequestMapping("logout")
    public void logout(
            ServiceRequest request,
            @ServiceResponseMessage SimpleServiceResponseMessage<Boolean> resp) throws Exception {
        String hstpAuth = request.getRequestHeaders().getHeader("hstp-auth");
        if(hstpAuth==null) return;
        String header = request.getRequestHeaders().getHeader("mcpdirect-device");
        int userDevice = header!=null?header.hashCode():0;
        Long userId = AIPortAccessKeyValidator.extractUserId(AIPortAccessKeyValidator.PREFIX_AUK, hstpAuth);
        if (userId != null) {
            SimpleServiceResponseMessage<Boolean> ar = new SimpleServiceResponseMessage<>();
            AccessKeyServiceHandler a = engine.getServiceRequestHandler(AccessKeyServiceHandler.class);
            a.remove(new AccessKeyServiceHandler.RequestOfVerify(userId, userDevice, hstpAuth), ar);
            AIToolMapper aiToolMapper = AIToolDataHelper.getInstance().getAIToolMapper();
            AIPortToolAgent agent = aiToolMapper.selectToolAgentByEngineId(userId,request.getRequestEngineId());
            boolean toolsUpdated = false;
            if(agent!=null){
                agent.status=0;
                aiToolMapper.updateToolAgentStatus(agent);
                toolsUpdated = aiToolMapper.updateToolAgentStatusOfTool(agent.id,agent.status)>0;
            }
            if(toolsUpdated) engine.broadcast(
                    USL.create("aitools@mcpdirect.ai/aitools/publish"),
                    "{\"tools\":[{\"userId\":"+userId+",\"lastUpdated\":"+System.currentTimeMillis()+"}]}");

            resp.success(ar.data);
        }
    }

    public static class RequestOfChangePassword{
        public String secretKey;
        public long timestamp;
        public String password;
    }

    @ServiceRequestMapping("password/change")
    public void changePassword(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfChangePassword req,
            @ServiceResponseMessage SimpleServiceResponseMessage<Boolean> resp) {
        AIPortAccountCredential credentials = accountMapper.selectUserAccountCredentialById(account.id);
        if (credentials != null) {
            String password = credentials.password.toLowerCase();
            if (!checkHash(req.secretKey, password, Long.toString(req.timestamp))) {
                resp.code = AIPortAccount.PASSWORD_INCORRECT;
                return;
            }
            accountMapper.updateUserAccountPassword(account.id, req.password);
            resp.success(true);
        }else{
            resp.code = AIPortAccount.ACCOUNT_NOT_EXIST;
        }
    }

    public static class RequestOfCreateAccessKey {
        public String name;
    }

    @ServiceRequestMapping("access_key/create")
    public void createAccessKey(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfCreateAccessKey req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortAccessKeyCredential> resp
    ) throws Exception {
        account = accountMapper.selectUserAccountById(account.id);
        if (account != null) {
            long now = System.currentTimeMillis();
//            String secretKey = AIPortAccessKeyGenerator.generateApiKey(
//                    AIPortAccessKeyValidator.PREFIX_AIK, account.id);
            String secretKey = AIPortAccessKeyGenerator.generateApiKey(
                    AIPortAccessKeyValidator.PREFIX_AIK, 0);
            AIPortAccessKeyCredential key = new AIPortAccessKeyCredential(
                    AIPortAccessKeyValidator.hashCode(secretKey),
                    account.id, Integer.MAX_VALUE, req.name,
                    AIPortAccessKeyGenerator.generateRandomKey(),
                    1, now, Long.MAX_VALUE,
                    now);
            accountMapper.insertAccessKeyCredential(key);
            key.secretKey = secretKey;
            resp.success(key);
        }
    }

    public static class RequestOfModifyAccessKey extends RequestOfCreateAccessKey{
        public int id;
        public Integer status;
    }

    @ServiceRequestMapping("access_key/modify")
    public void modifyAccessKey(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfModifyAccessKey req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortAccessKey> resp) throws Exception {
        account = accountMapper.selectUserAccountById(account.id);
        AIPortAccessKey accessKey;
        if (account != null && (accessKey = accountMapper.selectAccessKeyById(account.id,req.id))!=null) {
            int oldStatus = accessKey.status;
            if(req.status!=null&&req.status<0){
                accessKey.status = -1;
                accountMapper.deleteAccessKey(account.id,accessKey.id);
            }else {
                accessKey.name = (req.name!=null&&!(req.name= req.name.trim()).isEmpty())?req.name:null;
                if(accessKey.name!=null&&req.status!=null) {
                    accessKey.status = req.status;
                    accountMapper.updateAccessKey(accessKey);
                }else if(accessKey.name!=null){
                    accountMapper.updateAccessKeyName(accessKey);
                }else if(req.status!=null){
                    accessKey.status = req.status;
                    accountMapper.updateAccessKeyStatus(accessKey);
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
        public Integer keyId;
    }

    @ServiceRequestMapping("access_key/query")
    public void queryAccessKey(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfQueryAccessKey req,
            @ServiceResponseMessage SimpleServiceResponseMessage<List<AIPortAccessKey>> resp){
        if(req.keyId==null||req.keyId==0) {
            resp.success(accountMapper.selectAccessKeyByUserId(account.id));
        }else{
            AIPortAccessKey accessKey = accountMapper.selectAccessKeyById(account.id, req.keyId);
            resp.success(accessKey==null?List.of():List.of(accessKey));
        }
    }

    public static class RequestOfTransferAnonymous {
        public long id;
        public String secretKey;
        public long timestamp;
    }

    @ServiceRequestMapping("anonymous/transfer")
    public void transferAnonymous(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfTransferAnonymous req,
            @ServiceResponseMessage SimpleServiceResponseMessage<Boolean> resp) throws Exception {
        if(req.id !=0&&accountMapper.selectUserAnonymousCredential(req.id)!=null) {
            helper.executeSql(sqlSession -> {
                AccountMapper accountMapper = sqlSession.getMapper(AccountMapper.class);

                accountMapper.deleteUserAccount(req.id);
                accountMapper.deleteUserAnonymous(req.id);
                accountMapper.transferAccessKeys(req.id,account.id);

                AIToolMapper mapper = sqlSession.getMapper(AIToolMapper.class);
                List<AIPortToolAgent> anonymousAgents = mapper.selectToolAgentsByUserId(req.id);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddzHHmmss.SSS");
                Date now = new Date(System.currentTimeMillis());
                if(!anonymousAgents.isEmpty()) {
                    List<AIPortToolAgent> agents = mapper.selectToolAgentsByUserId(account.id);
                    Map<String, AIPortToolAgent> collect = agents.stream().collect(
                            Collectors.toMap(a -> a.engineId, a -> a));

                    for (AIPortToolAgent anonymousAgent : anonymousAgents) {
                        AIPortToolAgent agent = collect.get(anonymousAgent.engineId);
                        if(agent==null){
                            mapper.transferToolAgent(req.id, account.id,anonymousAgent.id);
                        }else{
                            mapper.transferToolMakers(anonymousAgent.id,agent.id,"-"+formatter.format(now));
                            mapper.transferTools(anonymousAgent.id,agent.id);
                        }
                    }
                    mapper.transferToolPermissions(req.id, account.id);
                    mapper.deleteToolAgentsByUserId(req.id);
//                    mapper.transferToolAgents(req.id, account.id);
//                    mapper.transferTools(req.id, account.id);
                }
                return true;
            });
            resp.success(true);
        }else{
            resp.success(false);
        }
    }

    public static class RequestOfCreateTeam{

    }
    @ServiceRequestMapping("team/create")
    public void createTeam(){

    }
    @ServiceRequestMapping("team/query")
    public void queryTeam(){

    }
    @ServiceRequestMapping("team/update")
    public void updateTeam(){

    }
    public void inviteTeamMember(){

    }
}
