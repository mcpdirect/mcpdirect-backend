package ai.mcpdirect.backend.service;

import ai.mcpdirect.backend.dao.AIToolDataHelper;
import ai.mcpdirect.backend.dao.entity.account.AIPortAccount;
import ai.mcpdirect.backend.dao.entity.aitool.*;
import ai.mcpdirect.backend.dao.mapper.aitool.AIToolMapper;
import ai.mcpdirect.backend.util.ID;
import appnet.hstp.*;
import appnet.hstp.annotation.*;
import appnet.hstp.exception.USLSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static ai.mcpdirect.backend.dao.entity.aitool.AIPortToolMaker.TYPE_MCP;


@ServiceName("aitools.management")
@ServiceRequestMapping("/tool_agent/")
public class AIToolAgentServiceHandler extends ServiceRequestAuthenticationHandler{
    private static final Logger LOG = LoggerFactory.getLogger(AIToolAgentServiceHandler.class);
    private static final USL publishBroadcastUSL = new USL("aitools","mcpdirect.ai","aitools/publish");

    private AIToolDataHelper helper;
    private AIToolMapper toolMapper;

    @ServiceRequestInit
    public void init(ServiceEngine engine) throws USLSyntaxException {
        this.engine = engine;
        helper = AIToolDataHelper.getInstance();
        toolMapper = helper.getAIToolMapper();

    }
    public static class RequestOfPublishTools{
        public AIPortToolMaker maker;
        public AIPortMCPServerConfig mcpServerConfig;
        public List<AIPortTool> tools;
    }
    @ServiceRequestMapping("tools/publish")
    public void publishTools(
            @ServiceRequestAuthentication("auk")
            AIPortAccount account,
            @ServiceRequestMessage RequestOfPublishTools req,
            @ServiceResponseMessage SimpleServiceResponseMessage<Long> resp
    ) throws Exception {
        AIPortToolAgent agent;
        if(req.maker.agentId>0
                && (agent = toolMapper.selectToolAgentById(req.maker.agentId))!=null
                && agent.userId==account.id){
            AIPortToolMaker m = null;
            boolean toolsUpdated = false;
            long now = System.currentTimeMillis();
            if(req.maker.id>0){
                if(req.tools!=null&&!req.tools.isEmpty()&&(m=toolMapper.selectToolMakerById(req.maker.id))!=null) {
                    try {
                        for (AIPortTool tool : req.tools) {
                            tool.agentStatus = agent.status;
                            tool.makerStatus = m.status;
                            AIPortTool old = toolMapper.selectToolByName(req.maker.id,tool.name);
                            if (old == null) {
                                tool.id = ID.nextId();
                                tool.userId = account.id;
                                tool.makerId = req.maker.id;
                                tool.makerStatus = 1;
                                tool.agentId = agent.id;
                                tool.agentStatus = agent.status;
                                tool.lastUpdated = now;
                                tool.status = 1;
                                if(tool.tags==null||(tool.tags=tool.tags.trim()).isEmpty())
                                    tool.tags="";
                                toolMapper.insertTool(tool);
                            } else if (old.hash != tool.hash) {
                                tool.id = old.id;
                                tool.lastUpdated = now;
                                toolMapper.updateToolMetaData(tool);
                            }
                        }

                    } catch (Exception e) {
                        LOG.warn("updateToolMetaData()",e);
                    }
                    toolsUpdated = true;
                }
            }else if((m = toolMapper.selectToolMakerByName(req.maker.agentId, req.maker.name))==null){
                toolMapper.selectToolAgentById(req.maker.agentId);
                req.maker.id = ID.nextId();
                req.maker.status = 1;
                req.maker.created = now;
                req.maker.userId = account.id;
                req.maker.lastUpdated = now;
                toolMapper.insertToolMaker(req.maker);
                m = req.maker;
                if(m.type==TYPE_MCP&&req.mcpServerConfig!=null){
                    req.mcpServerConfig.id = m.id;
                    toolMapper.insertMCPServerConfig(req.mcpServerConfig);
                    toolsUpdated = true;
                }
                if(req.tools!=null) try{
                    for (AIPortTool tool : req.tools) {
                        tool.id = ID.nextId();
                        tool.userId = account.id;
                        tool.makerId = req.maker.id;
                        tool.makerStatus = 1;
                        tool.agentId = agent.id;
                        tool.agentStatus = agent.status;
                        tool.lastUpdated = req.maker.created;
                        tool.status = 1;
                        if(tool.tags==null||(tool.tags=tool.tags.trim()).isEmpty()||
                                !tool.tags.startsWith("[")||!tool.tags.endsWith("]"))
                            tool.tags="[]";
                        toolMapper.insertTool(tool);
                    }
                }catch (Exception e){}
            }
            if(toolsUpdated) {
                engine.broadcast(
                        USL.create("aitools@mcpdirect.ai/aitools/publish"),
                        "{\"tools\":[{\"userId\":"+account.id+",\"lastUpdated\":"+now+"}]}");
            }
            if(m!=null){
                resp.success(m.id);
            }
        }

    }
    @ServiceRequestMapping("tools/unpublish")
    public void unpublishTools(
            @ServiceRequestAuthentication("auk")
            AIPortAccount account,
            @ServiceRequestMessage RequestOfPublishTools req,
            @ServiceResponseMessage SimpleServiceResponseMessage<Boolean> resp
    ){
        AIPortToolMaker m;
        if(req.maker.id>0&&(m = toolMapper.selectToolMakerById(req.maker.id))!=null) {
            List<AIPortTool> tools = toolMapper.selectToolsByMakerId(m.id);
            if(!tools.isEmpty()){
                List<Long> list = tools.stream().map(t -> t.id).toList();
                toolMapper.deleteToolPermissionsByIdList(list);
                toolMapper.deleteToolsByMakerId(m.id);
            }
            toolMapper.deleteToolMaker(m.id);
            resp.success(true);
            engine.broadcast(publishBroadcastUSL,
                    "{\"tools\":[{\"userId\":"+account.id+",\"lastUpdated\":"+System.currentTimeMillis()+"}]}");
        }else{
            resp.success(false);
        }
    }
    public static class ToolAgentDetails {
        public AIPortToolAgent toolAgent;
        public List<AIPortToolMaker> makers;
        public List<AIPortMCPServerConfig> mcpServerConfigs;

        public List<AIPortTool> tools;
    }
    @ServiceRequestMapping("init")
    public void initToolAgent(
            ServiceRequest request,
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage AIPortToolAgent req,
            @ServiceResponseMessage SimpleServiceResponseMessage<ToolAgentDetails> resp
    ) throws Exception {
        String engineId = request.getRequestEngineId();
        AIPortToolAgent agent = toolMapper.selectToolAgentByEngineId(account.id,engineId);
        ToolAgentDetails toolAgentDetails = new ToolAgentDetails();
        long now = System.currentTimeMillis();
        if(agent==null){
            if(req.device==null||(req.device=req.device.trim()).isEmpty()){
                req.device = "Unknow";
            }
            if(req.name==null||(req.name=req.name.trim()).isEmpty()){
                req.name = req.device;
            }
            agent = new AIPortToolAgent(
                    account.id,engineId, now,req.deviceId,
                    req.device,req.name,req.tags,1
            );
            toolMapper.insertToolAgent(agent);
            toolAgentDetails.toolAgent =agent;
            resp.success(toolAgentDetails);
        }else{
            agent.status=1;
            toolMapper.updateToolAgentStatus(agent);
            boolean toolsUpdated = toolMapper.updateToolAgentStatusOfTool(agent.id,agent.status)>0;
            if(toolsUpdated) engine.broadcast(
                    USL.create("aitools@mcpdirect.ai/aitools/publish"),
                    "{\"tools\":[{\"userId\":"+account.id+",\"lastUpdated\":"+now+"}]}");

            toolAgentDetails.toolAgent = agent;
            toolAgentDetails.makers = toolMapper.selectToolMakerByAgentId(agent.id);
            if(!toolAgentDetails.makers.isEmpty()) {
                List<Long> list = toolAgentDetails.makers.stream().map(m -> m.id).toList();
                toolAgentDetails.mcpServerConfigs = toolMapper.selectMCPServerConfigByMakerIds(list);
                toolAgentDetails.tools = toolMapper.selectToolsByMakerIds(list);
            }
            resp.success(toolAgentDetails);
        }
    }

    public static class RequestOfModifyAgent{
        public long agentId;
        public String name;
        public String tags;
        public Integer status;
    }
    @ServiceRequestMapping("modify")
    public void modifyAgent(
            ServiceRequest request,
            @ServiceRequestAuthentication("auk")
            AIPortAccount account,
            @ServiceRequestMessage RequestOfModifyAgent req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortToolAgent> resp
    ){
        AIPortToolAgent a;
        if(req.agentId>0&&(a=toolMapper.selectToolAgentById(req.agentId))!=null&&a.userId==account.id){
            if(req.name!=null&&!(req.name=req.name.trim()).isEmpty()){
                a.name = req.name;
                toolMapper.updateToolAgentName(a);
            }
            if(req.tags!=null&&!(req.tags=req.tags.trim()).isEmpty()){
                a.tags = req.tags;
                toolMapper.updateToolAgentTags(a);
            }
            if(req.status!=null){
                a.status = req.status;
                toolMapper.updateToolAgentStatus(a);
            }
            resp.success(a);
        }
    }

    public static class RequestOfToolAgentDetailsGet {
        public long agentId;
    }
    @ServiceRequestMapping("details/get")
    public void getToolAgentDetails(
            ServiceRequest request,
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfToolAgentDetailsGet req,
            @ServiceResponseMessage SimpleServiceResponseMessage<ToolAgentDetails> resp
    ) throws Exception {
        AIPortToolAgent agent;
        if(req.agentId!=0){
            agent = toolMapper.selectToolAgentById(req.agentId);
        }else {
            String engineId = request.getRequestEngineId();
            agent = toolMapper.selectToolAgentByEngineId(account.id, engineId);
        }
        if(agent!=null){
            ToolAgentDetails toolAgentDetails = new ToolAgentDetails();
            toolAgentDetails.toolAgent = agent;
            toolAgentDetails.makers = toolMapper.selectToolMakerByAgentId(agent.id);
            if(!toolAgentDetails.makers.isEmpty()) {
                List<Long> list = toolAgentDetails.makers.stream().map(m -> m.id).toList();
                toolAgentDetails.mcpServerConfigs = toolMapper.selectMCPServerConfigByMakerIds(list);
                toolAgentDetails.tools = toolMapper.selectToolsByMakerIds(list);
            }
            resp.success(toolAgentDetails);
        }
    }

    public static class AllToolAgentsDetails {
        public List<AIPortToolAgent> agents;
        public List<AIPortToolMaker> makers;

        public List<AIPortTool> tools;
        public List<AIPortToolPermission> permissions;
    }

    @ServiceRequestMapping("all/details/query")
    public void queryToolAgentDetails(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceResponseMessage SimpleServiceResponseMessage<AllToolAgentsDetails> resp
    ){
        List<AIPortToolAgent> agents = toolMapper.selectToolAgentsByUserId(account.id);
        List<AIPortToolMaker> makers=null;
        List<AIPortTool> tools=null;
        if(!agents.isEmpty()) {
            makers = toolMapper.selectToolMakerByAgentIds(
                    agents.stream().map(agent -> agent.id).toList());
            if(!makers.isEmpty()){
                 tools = toolMapper.selectToolsByMakerIds(
                        makers.stream().map(maker -> maker.id).toList());
            }
        }
        AllToolAgentsDetails toolAgentsDetails = new AllToolAgentsDetails();
        toolAgentsDetails.agents = agents;
        toolAgentsDetails.makers = makers;
        toolAgentsDetails.tools = tools;
        toolAgentsDetails.permissions = toolMapper.selectToolPermissionsByUserId(account.id);
        resp.success(toolAgentsDetails);
    }

    @ServiceRequestMapping("query")
    public void queryToolAgents(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceResponseMessage SimpleServiceResponseMessage<List<AIPortToolAgent>> resp
    ){
        resp.success(toolMapper.selectToolAgentsByUserId(account.id));
    }

    public static class RequestOfGetToolAgent{
        public long toolAgentId;
    }
    @ServiceRequestMapping("get")
    public void getToolAgent(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfGetToolAgent req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortToolAgent> resp
    ){
        resp.success(toolMapper.selectToolAgentById(req.toolAgentId));
    }
}
