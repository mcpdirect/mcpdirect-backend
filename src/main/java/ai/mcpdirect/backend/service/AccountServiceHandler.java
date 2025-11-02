package ai.mcpdirect.backend.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ai.mcpdirect.backend.dao.entity.account.*;
import ai.mcpdirect.backend.util.ID;
import appnet.hstp.*;
import appnet.hstp.annotation.*;

import ai.mcpdirect.backend.dao.AIToolDataHelper;
import ai.mcpdirect.backend.dao.AccountDataHelper;
import ai.mcpdirect.backend.dao.entity.aitool.AIPortToolAgent;
import ai.mcpdirect.backend.dao.mapper.account.AccountMapper;
import ai.mcpdirect.backend.dao.mapper.aitool.AIToolMapper;
import ai.mcpdirect.backend.util.AIPortAccessKeyGenerator;
import ai.mcpdirect.backend.util.AIPortAccessKeyValidator;

import static ai.mcpdirect.backend.service.AuthenticationServiceHandler.checkHash;

@ServiceName("account.management")
@ServiceRequestMapping("/")
public class AccountServiceHandler extends ServiceRequestAuthenticationHandler implements AccountServiceErrors{
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
            user = accountMapper.selectUserById(account.id);
        }
        resp.success(new UserInfo(user, account));
    }
    @ServiceRequestMapping("logout")
    public void logout(
            ServiceRequest request,
            @ServiceRequestAuthentication("auk") AIPortAccount account,
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
                resp.code = AccountServiceErrors.PASSWORD_INCORRECT;
                return;
            }
            accountMapper.updateUserAccountPassword(account.id, req.password);
            resp.success(true);
        }else{
            resp.code = AccountServiceErrors.ACCOUNT_NOT_EXIST;
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
//                    AIPortAccessKeyGenerator.generateRandomKey(),
                    secretKey,
                    1, now, Long.MAX_VALUE,
                    now);
            accountMapper.insertAccessKeyCredential(key);
//            key.secretKey = secretKey;
            resp.success(key);
        }
    }

    public static class RequestOfModifyAccessKey extends RequestOfCreateAccessKey{
        public long id;
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
        public Long keyId;
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

    @ServiceRequestMapping("access_key/credential/get")
    public void getAccessKeyCredential(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfQueryAccessKey req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortAccessKeyCredential> resp){
        if(req.keyId!=null&&req.keyId>0) {
            resp.success(accountMapper.selectAccessKeyCredentialById(req.keyId));
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
    public static class RequestOfGetUserAccount{
        public long userId;
    }
    @ServiceRequestMapping("user/get")
    public void getUserAccount(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfGetUserAccount req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortUser> resp
    ){
        if(req.userId>Integer.MAX_VALUE) resp.success(accountMapper.selectUserById(req.userId));
    }
    public static class RequestOfCreateTeam{
        public String name;
    }
    @ServiceRequestMapping("team/create")
    public void createTeam(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfCreateTeam req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortTeam> resp
    ){
        if(req.name!=null&&!(req.name=req.name.trim()).isEmpty()){
            long now = System.currentTimeMillis();
            AIPortTeam aiPortTeam = AIPortTeam.build()
                    .id(ID.nextId())
                    .name(req.name)
                    .ownerId(account.id)
                    .status(1)
                    .created(now)
                    .lastUpdated(now);
            AIPortTeamMember aiPortTeamMember = AIPortTeamMember.build()
                    .teamId(aiPortTeam.id)
                    .memberId(account.id)
                    .status(1)
                    .created(now)
                    .expirationDate(Long.MAX_VALUE)
                    .lastUpdated(now);
            accountMapper.insertTeamMember(aiPortTeamMember);
            accountMapper.insertTeam(aiPortTeam);
            resp.success(aiPortTeam);
        }
    }
    @ServiceRequestMapping("team/query")
    public void queryTeam(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceResponseMessage SimpleServiceResponseMessage<List<AIPortTeam>> resp
    ){
        List<AIPortTeam> list = new ArrayList<>(accountMapper.selectTeamsByOwnerId(account.id));
        list.addAll(accountMapper.selectTeamsByMemberId(account.id));
        resp.success(list);
    }

    public static class RequestOfModifyTeam{
        public long id;
        public String name;
        public Integer status;
    }

    @ServiceRequestMapping("team/modify")
    public void modifyTeam(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfModifyTeam req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortTeam> resp
    ){
        if(req.id>0 &&((req.name!=null&&!(req.name=req.name.trim()).isEmpty())
                ||req.status!=null) &&accountMapper.updateTeam(
                        AIPortTeam.build()
                                .id(req.id)
                                .ownerId(account.id)
                                .name(req.name)
                                .status(req.status)
                                .lastUpdated(System.currentTimeMillis())
        )>0) {
            resp.success(accountMapper.selectTeamById(req.id));
        }
    }

    public static class RequestOfInviteTeamMember{
        public String account;
        public long teamId;
    }
    @ServiceRequestMapping("team/member/invite")
    public void inviteTeamMember(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfInviteTeamMember req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortTeamMember> resp
    ){
        if(req.account!=null&&req.teamId>0) {
            AIPortUser u = accountMapper.selectUserByAccount(req.account);
            if(u==null){
                resp.code = ACCOUNT_NOT_EXIST;
            }else if(u.id!=account.id){
                AIPortTeam t = accountMapper.selectTeamById(req.teamId);
                if(t==null){
                    resp.code = TEAM_NOT_EXIST;
                }else {
                    long now = System.currentTimeMillis();
                    AIPortTeamMember m = AIPortTeamMember.build()
                            .teamId(req.teamId)
                            .memberId(u.id)
                            .status(1)
                            .created(now)
                            .lastUpdated(now)
                            .expirationDate(Long.MIN_VALUE+1);
                    accountMapper.insertTeamMember(m);
                    m.account = req.account;
                    m.name = u.name;
                    resp.success(m);
                }
            }
        }
    }

    public static class RequestOfAcceptTeamMember{
        public long teamId;
        public long memberId;
    }
    @ServiceRequestMapping("team/member/accept")
    public void acceptTeamMember(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfAcceptTeamMember req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortTeamMember> resp
    ){
        AIPortTeamMember m;
        if(account.id==req.memberId&&(m=accountMapper.selectTeamMemberById(req.teamId,req.memberId))!=null){
            m.expirationDate = Math.abs(m.expirationDate);
            accountMapper.updateTeamMember(m);
            resp.success(m);
        }
    }

    public static class RequestOfQueryTeamMember{
        public long teamId;
    }
    @ServiceRequestMapping("team/member/query")
    public void queryTeamMembers(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfQueryTeamMember req,
            @ServiceResponseMessage SimpleServiceResponseMessage<List<AIPortTeamMember>> resp
    ){
        if(req.teamId>0&&(accountMapper.selectTeamById(req.teamId))!=null) {
            resp.success(accountMapper.selectTeamMembersByTeamId(req.teamId));
        }else{
            resp.code = TEAM_NOT_EXIST;
        }
    }

    public static class RequestOfGetTeamMember{
        public long teamId;
        public long memberId;
    }
    @ServiceRequestMapping("team/member/get")
    public void getTeamMember(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfGetTeamMember req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortTeamMember> resp
    ){
        if(req.teamId>0&&(accountMapper.selectTeamMemberById(req.teamId,account.id))!=null) {
            resp.success(accountMapper.selectTeamMemberById(req.teamId,req.memberId));
        }else{
            resp.code = TEAM_NOT_EXIST;
        }
    }

    public static class RequestOfModifyTeamMember{
        public long memberId;
        public long teamId;
        public Integer status;
        public Long expirationDate;
    }
    @ServiceRequestMapping("team/member/modify")
    public void modifyTeamMember(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfModifyTeamMember req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortTeamMember> resp
    ){
        if(req.memberId>0&&req.teamId>0&&(accountMapper.selectTeamById(req.teamId))!=null) {
            if(accountMapper.updateTeamMember(
                    AIPortTeamMember.build()
                            .teamId(req.teamId)
                            .memberId(req.memberId)
                            .status(req.status)
                            .expirationDate(req.expirationDate)
                            .lastUpdated(System.currentTimeMillis())
            )>0){
                resp.success(accountMapper.selectTeamMemberById(req.teamId,req.memberId));
            }

        }
    }
}
