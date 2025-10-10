package ai.mcpdirect.backend.admin.service;

import ai.mcpdirect.backend.dao.entity.aitool.*;
import ai.mcpdirect.backend.util.ID;
import appnet.hstp.*;
import appnet.hstp.annotation.*;
import appnet.hstp.exception.USLSyntaxException;
import ai.mcpdirect.backend.dao.AIToolDataHelper;
import ai.mcpdirect.backend.dao.AccountDataHelper;
import ai.mcpdirect.backend.dao.entity.account.AIPortAccount;
import ai.mcpdirect.backend.dao.mapper.account.AccountMapper;
import ai.mcpdirect.backend.dao.mapper.aitool.AIToolMapper;
import ai.mcpdirect.backend.util.AIPortAuthenticationCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;


@ServiceName("aitools.management")
@ServiceRequestMapping("/tool/")
public class AIToolServiceHandler extends ServiceRequestAuthenticationHandler{
    private static final Logger LOG = LoggerFactory.getLogger(AIToolServiceHandler.class);
    private static final USL publishBroadcastUSL = new USL("aitools","mcpdirect.ai","aitools/publish");

    private final AIPortAuthenticationCache cache = new AIPortAuthenticationCache();

//    private ServiceEngine engine;
    private AIToolDataHelper helper;
    private AccountMapper accountMapper;
    private AIToolMapper toolMapper;

    @ServiceRequestInit
    public void init(ServiceEngine engine) throws USLSyntaxException {
        this.engine = engine;
        helper = AIToolDataHelper.getInstance();
        accountMapper = AccountDataHelper.getInstance().getAccountMapper();
        toolMapper = helper.getAIToolMapper();
    }

    public static class RequestOfGrantToolPermission{
        public List<AIPortToolPermission> permissions;
        public List<AIPortVirtualToolPermission> virtualPermissions;
    }
    @ServiceRequestMapping("permission/grant")
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
                for (AIPortVirtualToolPermission permission : req.virtualPermissions) {
                    permission.userId = account.id;
                    permission.lastUpdated = now;
                    int status = permission.status;
                    if(status==Short.MAX_VALUE){
                        permission.status = 1;
                        mapper.insertVirtualToolPermission(permission);
                    }else if(status==0||status==1){
                        mapper.updateVirtualToolPermission(permission);
                    }
                }
                return true;
            });
            req.permissions.addAll(req.virtualPermissions);
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

    public static class RequestOfQueryTools{
        public Long toolId;
        public String name;
        public Long agentId;
        public Long makerId;
        public Integer status;

    }
    @ServiceRequestMapping("query")
    public void queryTools(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfQueryTools req,
            @ServiceResponseMessage SimpleServiceResponseMessage<List<AIPortTool>> resp
    ) throws Exception {
        if(req.toolId!=null&&req.toolId>0) resp.success(List.of(toolMapper.selectToolById(req.toolId)));
        else resp.success(toolMapper.selectTools(account.id,req.status,req.agentId,req.makerId,req.name));
    }

    public static class RequestOfModifyVirtualTools{
        public long makerId;
        public List<AIPortVirtualTool> tools;
    }
    @ServiceRequestMapping("virtual/modify")
    public void modifyVirtualTools(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfModifyVirtualTools req,
            @ServiceResponseMessage SimpleServiceResponseMessage<List<AIPortVirtualTool>> resp
    ) throws Exception {
        if(req.makerId>0&&req.tools!=null) {
            for (AIPortVirtualTool tool : req.tools) {
                tool.makerId = req.makerId;
                tool.lastUpdated = System.currentTimeMillis();
                if (tool.id == 0 && tool.toolId > 0) {
                    tool.id = ID.nextId();
                    tool.status = 1;
                    tool.makerStatus = 1;
                    toolMapper.insertVirtualTool(tool);
                } else if (tool.id > 0) {
                    toolMapper.updateVirtualToolStatus(tool);
                }
            }
            resp.success(toolMapper.selectVirtualToolByMakerId(req.makerId));
        }
    }

    public static class RequestOfQueryVirtualTools{
        public long makerId;
    }
    @ServiceRequestMapping("virtual/query")
    public void queryVirtualTools(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfQueryVirtualTools req,
            @ServiceResponseMessage SimpleServiceResponseMessage<List<AIPortVirtualTool>> resp
    ) throws Exception {
        if(req.makerId>0) {
            resp.success(toolMapper.selectVirtualToolByMakerId(req.makerId));
        }else{
            resp.success(toolMapper.selectVirtualTools(account.id));
        }
    }

    public static class RequestOfQueryToolPermissions{
        public long accessKeyId;
    }
    @ServiceRequestMapping("permission/query")
    public void queryToolPermissions(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfQueryToolPermissions req,
            @ServiceResponseMessage SimpleServiceResponseMessage<List<AIPortToolPermission>> resp
    ) throws Exception {
        if(req.accessKeyId>0) {
            resp.success(toolMapper.selectToolPermissionByAccessKey(account.id,req.accessKeyId));
        }
    }

    @ServiceRequestMapping("virtual/permission/query")
    public void queryVirtualTools(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfQueryToolPermissions req,
            @ServiceResponseMessage SimpleServiceResponseMessage<List<AIPortVirtualToolPermission>> resp
    ) throws Exception {
        if(req.accessKeyId>0) {
            resp.success(toolMapper.selectVirtualToolPermissionByAccessKey(account.id,req.accessKeyId));
        }
    }
}
