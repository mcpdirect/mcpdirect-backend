package ai.mcpdirect.backend.admin.service;

import ai.mcpdirect.backend.dao.AIToolDataHelper;
import ai.mcpdirect.backend.dao.entity.account.AIPortAccount;
import ai.mcpdirect.backend.dao.entity.aitool.*;
import ai.mcpdirect.backend.dao.mapper.aitool.AIToolMapper;
import ai.mcpdirect.backend.util.ID;
import appnet.hstp.*;
import appnet.hstp.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


@ServiceName("aitools.management")
@ServiceRequestMapping("/tool_maker/")
public class AIToolMakerServiceHandler extends ServiceRequestAuthenticationHandler{
    private static final Logger LOG = LoggerFactory.getLogger(AIToolMakerServiceHandler.class);

    private AIToolMapper toolMapper;

    @ServiceRequestInit
    public void init(ServiceEngine engine){
        this.engine = engine;
        toolMapper = AIToolDataHelper.getInstance().getAIToolMapper();
    }

    public static class RequestOfCreateToolMaker{
        public String name;
        public int type;
        public String tags;
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
        maker.name = req.name;
        maker.type = req.type;
        maker.tags = req.tags;
        maker.agentId = req.type == 0 ? account.id : Long.parseLong(request.getRequestEngineId());
        maker.status = 1;
        maker.created = System.currentTimeMillis();
        maker.lastUpdated = maker.created;
        toolMapper.insertToolMaker(maker);
    }

    public static class RequestOfModifyToolMaker{
        public long id;
        public String name;
        public String tags;
        public Integer status;
    }
    @ServiceRequestMapping("status/modify")
    public void modifyToolMaker(
            ServiceRequest request,
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfModifyToolMaker req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortToolMaker> resp
    ){
        if(req.id>0&&req.status!=null&&toolMapper.updateToolMakerStatus(req.id,req.status)>0) {
            resp.success(toolMapper.selectToolMakerById(req.id));
        }
    }

    @ServiceRequestMapping("name/modify")
    public void modifyToolMakerName(
            ServiceRequest request,
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfModifyToolMaker req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortToolMaker> resp
    ){
        if(req.id>0&&req.status!=null&&toolMapper.updateToolMakerName(req.id,req.name)>0) {
            resp.success(toolMapper.selectToolMakerById(req.id));
        }
    }

    @ServiceRequestMapping("tags/modify")
    public void modifyToolMakerTags(
            ServiceRequest request,
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfModifyToolMaker req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortToolMaker> resp
    ){
        if(req.id>0&&req.tags!=null&&toolMapper.updateToolMakerTags(req.id,req.tags)>0) {
            resp.success(toolMapper.selectToolMakerById(req.id));
        }
    }

    public static class RequestOfQueryToolMaker{
        public String name;
        public Integer type;
        public Long toolAgentId;
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
        List<AIPortToolMaker> list = new ArrayList<>();
        if(req.type==null||req.type==0){
            list.addAll(toolMapper.selectVirtualToolMakerByUserId(account.id,req.name));
        }
        if(req.type==null||req.type!=0){
            if(req.type!=null&&req.type==-1){
                req.type = null;
            }
            list.addAll(toolMapper.selectToolMakerByUserId(account.id,req.name,req.type,req.toolAgentId));
        }
        resp.success(list);
    }
}
