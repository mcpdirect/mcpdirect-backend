package ai.mcpdirect.backend.service;

import ai.mcpdirect.backend.dao.AIToolDataHelper;
import ai.mcpdirect.backend.dao.AccountDataHelper;
import ai.mcpdirect.backend.dao.entity.account.AIPortAccount;
import ai.mcpdirect.backend.dao.entity.account.AIPortTeam;
import ai.mcpdirect.backend.dao.entity.account.AIPortTeamMember;
import ai.mcpdirect.backend.dao.entity.aitool.*;
import ai.mcpdirect.backend.dao.mapper.account.AccountMapper;
import ai.mcpdirect.backend.dao.mapper.aitool.AIToolMapper;
import ai.mcpdirect.backend.util.ID;
import appnet.hstp.*;
import appnet.hstp.annotation.*;
import appnet.hstp.exception.USLSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


@ServiceName("aitools.management")
@ServiceRequestMapping("/tool_maker/")
public class AIToolMakerServiceHandler extends ServiceRequestAuthenticationHandler{
    private static final Logger LOG = LoggerFactory.getLogger(AIToolMakerServiceHandler.class);

    private AIToolMapper toolMapper;
    private AccountMapper accountMapper;

    @ServiceRequestInit
    public void init(ServiceEngine engine){
        this.engine = engine;
        toolMapper = AIToolDataHelper.getInstance().getAIToolMapper();
        accountMapper = AccountDataHelper.getInstance().getAccountMapper();
    }

    public static class RequestOfCreateToolMaker{
        public long templateId;
        public long userId;
        public long agentId;

        public String name;
        public int type;
        public String tags;
        public AIPortMCPServerConfig mcpServerConfig;
    }
    @ServiceRequestMapping("create")
    public void createToolMaker(
            ServiceRequest request,
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfCreateToolMaker req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortToolMaker> resp
    ){
        AIPortToolMaker maker = new AIPortToolMaker();
        maker.id = ID.nextId();
        maker.userId = account.id;
        maker.name(req.name);
        maker.tags(req.tags);

        if(req.type != AIPortToolMaker.TYPE_VIRTUAL) {
            if(req.type == AIPortToolMaker.TYPE_MCP&&req.mcpServerConfig==null){
                return;
            }
            AIPortToolAgent agent;
            if(req.templateId>Integer.MAX_VALUE) {
                maker.userId = req.userId;
                maker.templateId = req.templateId;
                agent = toolMapper.selectToolAgentById(req.agentId);
            } else agent = toolMapper.selectToolAgentByEngineId(account.id, request.getRequestEngineId());

//            if(agent==null||agent.userId!=account.id){
            if(agent==null){
                return;
            }
            maker.agentId = agent.id;
        }

        maker.type = req.type;
        maker.status = 1;
        maker.created = System.currentTimeMillis();
        maker.lastUpdated = maker.created;
        toolMapper.insertToolMaker(maker);

        if(req.type==AIPortToolMaker.TYPE_MCP){
            req.mcpServerConfig.id = maker.id;
            toolMapper.insertMCPServerConfig(req.mcpServerConfig);
        }
        resp.success(maker);
    }

    public static class RequestOfModifyToolMaker{
        public long makerId;
        public String name;
        public String tags;
        public Integer status;
    }
    @ServiceRequestMapping("modify")
    public void modifyToolMaker(
            ServiceRequest request,
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfModifyToolMaker req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortToolMaker> resp
    ) throws USLSyntaxException {
        AIPortToolMaker maker;
        if(req.makerId>0&&(maker=toolMapper.selectToolMakerById(req.makerId))!=null
                &&maker.userId==account.id) {
            long now = System.currentTimeMillis();
            boolean updated = false;
            if(req.name!=null&&!(req.name=req.name.trim()).isEmpty()){
                toolMapper.updateToolMakerName(req.makerId,req.name,now);
            }
            if(req.status!=null){
                updated = toolMapper.updateToolMakerStatus(req.makerId,req.status,now)>0;
            }
            if(req.tags!=null&&!(req.tags=req.tags.trim()).isEmpty()){
                updated = updated||toolMapper.updateToolMakerTags(req.makerId,req.tags,now)>0;
            }
            if(updated) engine.broadcast(
                    USL.create("aitools@mcpdirect.ai/aitools/publish"),
                    "{\"tools\":[{\"userId\":"+account.id+",\"lastUpdated\":"+now+"}]}");
            resp.success(toolMapper.selectToolMakerById(req.makerId));
        }
    }

    public static class RequestOfQueryToolMaker{
        public Integer type;
        public String name;
        public Long toolAgentId;
        public Long teamId;
        public long lastUpdated;
    }
    @ServiceRequestMapping("query")
    public void queryToolMakers(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfQueryToolMaker req,
            @ServiceResponseMessage SimpleServiceResponseMessage<List<AIPortToolMaker>> resp
    ){
        if(req.name!=null&&(req.name = req.name.trim()).isEmpty()){
            req.name = null;
        }
//        if(req.type==null||req.type==0){
//            list.addAll(toolMapper.selectVirtualToolMakerByUserId(account.id,req.name,req.lastUpdated));
//        }
//        if(req.type==null||req.type>0){
//            if(req.type!=null&&req.type==Integer.MAX_VALUE){
//                req.type = null;
//            }
//            list.addAll(toolMapper.selectToolMakersByUserId(account.id,req.name,req.type,
//                    req.toolAgentId,req.lastUpdated));
//        }
        List<AIPortToolMaker> list = new ArrayList<>(
                toolMapper.selectToolMakersByUserId(account.id, req.name, req.type,
                        req.toolAgentId, req.lastUpdated));

        if(req.teamId!=null){
            list.addAll(toolMapper.selectToolMakersByTeamId(req.teamId,req.lastUpdated));
        }else{
            list.addAll(toolMapper.selectToolMakersByTeamMemberId(account.id,req.lastUpdated));
        }
        resp.success(list);
    }

    public static class RequestOfGetToolMakerDetails{
        public long makerId;
    }
    public static class ToolMakerDetails{
        public AIPortToolMaker maker;
        public AIPortMCPServerConfig config;
        public List<AIPortTool> tools;
    }
    @ServiceRequestMapping("details/get")
    public void getToolMakerDetails(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfGetToolMakerDetails req,
            @ServiceResponseMessage SimpleServiceResponseMessage<ToolMakerDetails> resp
    ){
        if(req.makerId<Integer.MAX_VALUE){
            return;
        }
        ToolMakerDetails details = new ToolMakerDetails();
        details.maker = toolMapper.selectToolMakerById(req.makerId);
        if(details.maker==null){
            return;
        }
        details.config = toolMapper.selectMCPServerConfigById(req.makerId);
        details.tools = toolMapper.selectToolsByMakerId(req.makerId);

        resp.success(details);
    }

    public static class RequestOfModifyTeamToolMaker{
        public long teamId;
        public long teamOwnerId;
        public List<AIPortTeamToolMaker> teamToolMakers;
    }
    @ServiceRequestMapping("team/modify")
    public void modifyTeamToolMaker(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfModifyTeamToolMaker req,
            @ServiceResponseMessage SimpleServiceResponseMessage<List<AIPortTeamToolMaker>> resp
    ) throws Exception {

        long now = System.currentTimeMillis();
        if(req.teamId<1||req.teamOwnerId<1||req.teamToolMakers==null||req.teamToolMakers.isEmpty()){
            return;
        }
        AIPortTeamMember m;
        AIPortTeam t;
        boolean isOwner = req.teamOwnerId==account.id;
        if(isOwner&&((t=accountMapper.selectTeamById(req.teamId))==null||t.ownerId!=req.teamOwnerId)){
            return;
        }else if(!isOwner &&((m=accountMapper.selectTeamMemberById(req.teamId,account.id))==null
                ||m.status!=1||m.expirationDate<now)){
            return;
        }

            AIToolDataHelper.getInstance().executeSql(sqlSession -> {
                AIToolMapper mapper = sqlSession.getMapper(AIToolMapper.class);
                for (AIPortTeamToolMaker tmt : req.teamToolMakers) if(tmt.toolMakerId>0){
                    tmt.teamId = req.teamId;
                    tmt.lastUpdated = now;
                    int status = tmt.status;
                    if(status==Short.MAX_VALUE){
                        tmt.created = now;
                        tmt.status = 1;
                        mapper.insertTeamToolMaker(tmt);
                    }else if(status==0||status==1){
                        mapper.updateTeamToolMaker(tmt);
                    }
                }
                return true;
            });
            resp.success(toolMapper.selectTeamToolMakerByTeamId(req.teamId));
    }
    public static class RequestOfQueryTeamToolMaker{
        public long teamId;
        public long lastUpdated;
//        public long teamOwnerId;
    }
    @ServiceRequestMapping("team/query")
    public void queryTeamToolMakers(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfQueryTeamToolMaker req,
            @ServiceResponseMessage SimpleServiceResponseMessage<List<AIPortTeamToolMaker>> resp
    ) throws Exception {
        if(req.teamId==0){
            resp.success(toolMapper.selectTeamToolMakersByMemberId(account.id,req.lastUpdated));
        }else {
            AIPortTeamMember m;
            if (req.teamId < Integer.MAX_VALUE
                    || (m = accountMapper.selectTeamMemberById(req.teamId, account.id)) == null
                    || m.status != 1 || m.expirationDate < System.currentTimeMillis()) {
                return;
            }
            resp.success(toolMapper.selectTeamToolMakerByTeamId(req.teamId));
        }
    }

    public static class RequestOfGetMCPServerConfig{
        public long configId;
    }
    @ServiceRequestMapping("mcp_server_config/get")
    public void getMCPServerConfig(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfGetMCPServerConfig req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortMCPServerConfig> resp
    ) throws Exception {
        if(req.configId<Integer.MAX_VALUE){
            return;
        }
        resp.success(toolMapper.selectMCPServerConfigById(req.configId));
    }

    public static class RequestOfModifyMCPServerConfig{
        public AIPortMCPServerConfig mcpServerConfig;
    }

    @ServiceRequestMapping("mcp_server_config/modify")
    public void modifyMCPServerConfig(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfModifyMCPServerConfig req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortToolMaker> resp
    ) throws Exception {
        AIPortToolMaker maker;
        if(req.mcpServerConfig!=null&&req.mcpServerConfig.id>Integer.MAX_VALUE
                &&(maker=toolMapper.selectToolMakerById(req.mcpServerConfig.id))!=null
                &&maker.userId==account.id){
            toolMapper.updateMCPServerConfig(req.mcpServerConfig);
            resp.success(maker);
        }
    }
}
