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
import appnet.hstp.ServiceEngine;
import appnet.hstp.ServiceRequest;
import appnet.hstp.SimpleServiceResponseMessage;
import appnet.hstp.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@ServiceName("aitools.management")
@ServiceRequestMapping("/tool_maker/template/")
public class AIToolMakerTemplateServiceHandler extends ServiceRequestAuthenticationHandler{
    private static final Logger LOG = LoggerFactory.getLogger(AIToolMakerTemplateServiceHandler.class);

    private AIToolMapper toolMapper;
    private AccountMapper accountMapper;

    @ServiceRequestInit
    public void init(ServiceEngine engine){
        this.engine = engine;
        toolMapper = AIToolDataHelper.getInstance().getAIToolMapper();
        accountMapper = AccountDataHelper.getInstance().getAccountMapper();
    }

    public static class RequestOfCreateToolMakerTemplate{
        public String name;
        public int type;
        public String config;
        public String inputs;
        public long agentId;
    }
    @ServiceRequestMapping("create")
    public void createToolMakerTemplate(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfCreateToolMakerTemplate req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortToolMakerTemplate> resp
    ){
        AIPortToolMakerTemplate template = new AIPortToolMakerTemplate();
        template.id = ID.nextId();
        template.userId = account.id;
        template.name = req.name;
        template.type = req.type;
        template.config = req.config;
        template.inputs = req.inputs;
        template.agentId = req.agentId;
        template.status = 1;
        template.created = System.currentTimeMillis();
        template.lastUpdated = template.created;
        toolMapper.insertToolMakerTemplate(template);
        resp.success(template);
    }

    public static class RequestOfModifyToolMakerTemplate{
        public long templateId;
        public String name;
        public Integer status;
        public String config;
        public String inputs;
    }
    @ServiceRequestMapping("modify")
    public void modifyToolMakerTemplate(
            ServiceRequest request,
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfModifyToolMakerTemplate req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortToolMaker> resp
    ){
        if((req.name!=null||req.status!=null||req.config!=null||req.inputs!=null)
                &&toolMapper.updateToolMakerTemplate(
                req.templateId,account.id,
                req.name,req.status,
                req.config,req.inputs,
                System.currentTimeMillis())>0) {
            resp.success(toolMapper.selectToolMakerById(req.templateId));
        }
    }

    public static class RequestOfQueryToolMakerTemplate{
        public long lastUpdated;
    }
    @ServiceRequestMapping("query")
    public void queryToolMakerTemplates(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfQueryToolMakerTemplate req,
            @ServiceResponseMessage SimpleServiceResponseMessage<List<AIPortToolMakerTemplate>> resp
    ){
        List<AIPortToolMakerTemplate> list = toolMapper.selectToolMakerTemplateByUserId(account.id,req.lastUpdated);
        list.addAll(toolMapper.selectToolMakerTemplateByMemberId(account.id,req.lastUpdated));
        resp.success(list);
    }

    public static class RequestOfModifyTeamToolMakerTemplate{
        public long teamId;
        public List<AIPortTeamToolMakerTemplate> teamToolMakerTemplates;
    }
    @ServiceRequestMapping("team/modify")
    public void modifyTeamToolMaker(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfModifyTeamToolMakerTemplate req,
            @ServiceResponseMessage SimpleServiceResponseMessage<List<AIPortTeamToolMakerTemplate>> resp
    ) throws Exception {

        long now = System.currentTimeMillis();
        if(req.teamId<1||req.teamToolMakerTemplates==null||req.teamToolMakerTemplates.isEmpty()){
            return;
        }
        AIPortTeamMember m;
        if((m=accountMapper.selectTeamMemberById(req.teamId,account.id))==null
                ||m.status!=1||m.expirationDate<now){
            return;
        }

            AIToolDataHelper.getInstance().executeSql(sqlSession -> {
                AIToolMapper mapper = sqlSession.getMapper(AIToolMapper.class);
                for (AIPortTeamToolMakerTemplate tmt : req.teamToolMakerTemplates) if(tmt.toolMakerTemplateId>0){
                    tmt.teamId = req.teamId;
                    tmt.lastUpdated = now;
                    int status = tmt.status;
                    if(status==Short.MAX_VALUE){
                        tmt.created = now;
                        tmt.status = 1;
                        mapper.insertTeamToolMakerTemplate(tmt);
                    }else if(status==0||status==1){
                        mapper.updateTeamToolMakerTemplate(tmt);
                    }
                }
                return true;
            });
            resp.success(toolMapper.selectTeamToolMakerTemplatesByTeamId(req.teamId,0));
    }
    public static class RequestOfQueryTeamToolMaker{
        public long teamId;
        public long lastUpdated;
    }
    @ServiceRequestMapping("team/query")
    public void queryTeamToolMakerTemplates(
            @ServiceRequestAuthentication("auk") AIPortAccount account,
            @ServiceRequestMessage RequestOfQueryTeamToolMaker req,
            @ServiceResponseMessage SimpleServiceResponseMessage<List<AIPortTeamToolMakerTemplate>> resp
    ) throws Exception {
        if(req.teamId==0){
            resp.success(toolMapper.selectTeamToolMakerTemplatesByMemberId(account.id,
                    req.lastUpdated,System.currentTimeMillis()));
        }else {
            AIPortTeamMember m;
            if (req.teamId < Integer.MAX_VALUE
                    || (m = accountMapper.selectTeamMemberById(req.teamId, account.id)) == null
                    || m.status != 1 || m.expirationDate < System.currentTimeMillis()) {
                return;
            }
            resp.success(toolMapper.selectTeamToolMakerTemplatesByTeamId(req.teamId,0));
        }
    }
}
