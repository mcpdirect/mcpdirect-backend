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
        maker.userId = account.id;
        maker.name = req.name;
        maker.type = req.type;
        maker.tags = req.tags;
        maker.agentId = req.type == 0 ? account.id : Long.parseLong(request.getRequestEngineId());
        maker.status = 1;
        maker.created = System.currentTimeMillis();
        maker.lastUpdated = maker.created;
        toolMapper.insertToolMaker(maker);
        resp.success(maker);
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
        @ServiceRequestSchema(
                description = """
                        type = null means select all tool makers and virtual too makers,
                        type = 0 means select all virtual tool makers
                        type > 0 means select tool makers with the type"""
        )
        public Integer type;
        public String name;
        public Long toolAgentId;
        public Long teamId;
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
        if(req.type==null||req.type>0){
            if(req.type!=null&&req.type==Integer.MAX_VALUE){
                req.type = null;
            }
            list.addAll(toolMapper.selectToolMakerByUserId(account.id,req.name,req.type,req.toolAgentId));
        }
        if(req.teamId!=null){
            list.addAll(toolMapper.selectToolMakersByTeamId(req.teamId));
        }else{
            list.addAll(toolMapper.selectToolMakersByUserId(account.id));
        }
        resp.success(list);
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
        public long teamOwnerId;
    }
    @ServiceRequestMapping("team/query")
    public void queryTeamToolMakers(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfQueryTeamToolMaker req,
            @ServiceResponseMessage SimpleServiceResponseMessage<List<AIPortTeamToolMaker>> resp
    ) throws Exception {
        if(req.teamId<1||req.teamOwnerId<1){
            return;
        }
        AIPortTeamMember m;
        if(req.teamOwnerId!=account.id
                &&((m=accountMapper.selectTeamMemberById(req.teamId,account.id))==null
                ||m.status!=1||m.expirationDate<System.currentTimeMillis())){
            return;
        }
        resp.success(toolMapper.selectTeamToolMakerByTeamId(req.teamId));
    }
}
