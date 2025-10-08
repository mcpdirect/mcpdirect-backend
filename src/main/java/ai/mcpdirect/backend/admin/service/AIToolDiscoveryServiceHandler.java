package ai.mcpdirect.backend.admin.service;

import ai.mcpdirect.backend.dao.AIToolDataHelper;
import ai.mcpdirect.backend.dao.AccountDataHelper;
import ai.mcpdirect.backend.dao.entity.account.AIPortAccessKeyCredential;
import ai.mcpdirect.backend.dao.entity.account.AIPortAccount;
import ai.mcpdirect.backend.dao.entity.aitool.AIPortTool;
import ai.mcpdirect.backend.dao.entity.aitool.AIPortToolAgent;
import ai.mcpdirect.backend.dao.entity.aitool.AIPortToolPermission;
import ai.mcpdirect.backend.dao.entity.aitool.AIPortToolProvider;
import ai.mcpdirect.backend.dao.mapper.account.AccountMapper;
import ai.mcpdirect.backend.dao.mapper.aitool.AIToolMapper;
import ai.mcpdirect.backend.util.AIPortAccessKeyValidator;
import ai.mcpdirect.backend.util.AIPortAuthenticationCache;
import ai.mcpdirect.backend.util.AIToolsDirectory;
import appnet.hstp.*;
import appnet.hstp.annotation.*;
import appnet.hstp.engine.util.JSON;
import appnet.hstp.exception.USLSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@ServiceName("aitools.management")
@ServiceRequestMapping("/")
public class AIToolDiscoveryServiceHandler extends ServiceRequestAuthenticationHandler implements ServiceBroadcastListener{
    private static final Logger LOG = LoggerFactory.getLogger(AIToolDiscoveryServiceHandler.class);
    private static final USL publishBroadcastUSL = new USL("aitools","mcpdirect.ai","aitools/publish");

    private final AIPortAuthenticationCache cache = new AIPortAuthenticationCache();

//    private ServiceEngine engine;
    private AIToolDataHelper helper;
    private AccountMapper accountMapper;
    private AIToolMapper toolMapper;

    @Override
    public void onServiceBroadcastEvent(int i, USL usl, String s, String s1) {

    }

    @ServiceRequestInit
    public void init(ServiceEngine engine) throws USLSyntaxException {
        this.engine = engine;
        helper = AIToolDataHelper.getInstance();
        accountMapper = AccountDataHelper.getInstance().getAccountMapper();
        toolMapper = helper.getAIToolMapper();
        engine.joinBroadcastGroup(USL.create("aitools@mcpdirect.ai"),"",
                AUDIENCES_PEERS|AUDIENCES_LOCAL,this);
    }
    @ServiceRequestAuthentication
    public AIPortAccessKeyCredential authenticate(
            ServiceRequest request, Class<?> authObjectType, int[] authRoles, boolean anonymous){
        String aiportAuth = request.getRequestHeaders().getHeader("mcpdirect-auth");

        AIPortAccessKeyCredential accessKey = cache.get(aiportAuth);
        Long userId;
        if(accessKey==null&&(userId=AIPortAccessKeyValidator.extractUserId(
                AIPortAccessKeyValidator.PREFIX_AIK,aiportAuth))!=null
                &&(accessKey = accountMapper.selectAccessKeyCredentialById(AIPortAccessKeyValidator.hashCode(aiportAuth)))!=null){
            cache.add(accessKey);
        }
        return accessKey;
    }

    public static class RequestOfAnnounce{

        public List<ServiceDescription> descriptions;
        public long appId;
        public String device;
    }
    @ServiceRequestMapping("announce/tools")
    public void announce(
            ServiceRequest request,
            @ServiceRequestAuthentication AIPortAccessKeyCredential key,
            @ServiceRequestMessage RequestOfAnnounce req,
            @ServiceResponseMessage SimpleServiceResponseMessage<Object> resp
    ) throws Exception {
        if(req.descriptions!=null) {
            if(req.device==null){
                req.device="";
            }
            String json = JSON.toJson(req.descriptions);
            long hash = AIPortAccessKeyValidator.hashCode(json);
            String providerId = request.getRequestEngineId();
            Long oldHash = toolMapper.selectToolsProviderHash(key.userId,providerId);
            int r = 0;
            if(oldHash==null){
                r = toolMapper.insertToolsProvider(new AIPortToolProvider(
                        key.userId, providerId,
                        json,hash,
                        System.currentTimeMillis(),req.appId,
                        req.device
                ));
            }else if(!oldHash.equals(hash)){
                r = toolMapper.updateToolsProviderByProviderId(
                        key.userId, providerId,
                        json,hash,
                        System.currentTimeMillis()
                );
            }
            if(r>0) {
                engine.broadcast(
                        USL.create("aitools@mcpdirect.ai/aitools/announce"),
                        "{\"users\":["+key.userId+"]}");
            }
            resp.success();
        }
    }

    @ServiceRequestMapping("list/user/tools")
    public void listUserTools(
            @ServiceRequestAuthentication AIPortAccessKeyCredential key,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIToolsDirectory> resp
    ) throws Exception {
        AIToolsDirectory directory = AIToolsDirectory.create(key.userId);
        List<AIPortTool> aiPortTools = toolMapper.selectPermittedTools(key.id);
        List<Long> agentIds = aiPortTools.stream().map(t -> t.agentId).toList();
        if(agentIds.isEmpty()){
            directory.tools = Map.of();
            resp.success(directory);
            return;
        }
        Map<Long, AIPortToolAgent> agentMap = toolMapper.selectToolAgentByIds(agentIds).stream()
                .collect(Collectors.toMap(a -> a.id, a -> a));

        long agentId = 0;
        AIToolsDirectory.Tools tools=null;
        for (AIPortTool tool : aiPortTools) {
            if(tool.agentId!=agentId){
                agentId = tool.agentId;
                if(tools!=null){
                    directory.tools.put(tools.engineId,tools);
                }
                tools = null;
            }
            if(tools==null){
                tools = new AIToolsDirectory.Tools();
                tools.descriptions = new ArrayList<>();
                tools.engineId = agentMap.get(agentId).engineId;
            }
            AIToolsDirectory.Description d = new AIToolsDirectory.Description();
            d.name = tool.name;
            d.tags = tool.tags;
            d.metaData = tool.metaData;
            tools.descriptions.add(d);
        }
        if(tools!=null){
            directory.tools.put(tools.engineId,tools);
        }
        resp.success(directory);
    }

    public static class RequestOfGrantToolPermission{
        public List<AIPortToolPermission> permissions;
    }
    @ServiceRequestMapping("tool/permission/grant")
    public void grantToolPermission(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfGrantToolPermission req,
            @ServiceResponseMessage SimpleServiceResponseMessage<List<AIPortToolPermission>> resp
    ) throws Exception {
        if(!req.permissions.isEmpty()) {
            long now = System.currentTimeMillis();
//            List<Integer> keyList = permissions.stream().map(p -> p.accessKeyId).toList();
//            List<Long> agentList = permissions.stream().map(p -> p.agentId).toList();
//            List<Long> makerList = permissions.stream().map(p -> p.makerId).toList();
//            AccountDataHelper accountDataHelper = AccountDataHelper.getInstance();
//            List<AIPortAccessKey> keys = accountDataHelper.getAccountMapper().selectAccessKeyByIds(account.id, keyList);
//            Map<Integer, AIPortAccessKey> keyMap = keys.stream().collect(Collectors.toMap(key -> key.id, key -> key));
//            List<AIPortToolsAgent> agents = toolMapper.selectToolsAgentByIds(agentList);
//            Map<Long, AIPortToolsAgent> agentMap = agents.stream().collect(Collectors.toMap(agent -> agent.id, agent -> agent));
//            List<AIPortToolsMaker> makers = toolMapper.selectToolsMakerByIds(makerList);
//            Map<Long, AIPortToolsMaker> makerMap = makers.stream().collect(Collectors.toMap(maker -> maker.id, maker -> maker));
            helper.executeSql(sqlSession -> {
                AIToolMapper mapper = sqlSession.getMapper(AIToolMapper.class);
                for (AIPortToolPermission permission : req.permissions) {
                    permission.userId = account.id;
                    permission.lastUpdated = now;
//                permission.userStatus = account.status;
//                permission.accessKeyStatus = keyMap.get(permission.accessKeyId).status;
//                permission.agentStatus = agentMap.get(permission.agentId).status;
//                permission.makerStatus = makerMap.get(permission.makerId).status;
                    int status = permission.status;
                    if(status==Short.MAX_VALUE){
                        permission.status = 1;
                        mapper.insertToolPermission(permission);
                    }else if(status==0||status==1){
                        mapper.updateToolPermission(permission);
                    }
                }
                return true;
            });

            resp.success(req.permissions);
            try {
                engine.broadcast(
                        publishBroadcastUSL,
                        "{\"tools\":[{\"userId\":" + account.id + ",\"lastUpdated\":" + now + "}]}");
            }catch (Exception e){
                LOG.error("broadcast()",e);
            }
        }
    }
}
