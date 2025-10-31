package ai.mcpdirect.backend.dao.mapper.aitool;

import ai.mcpdirect.backend.dao.entity.aitool.AIPortToolMakerTemplate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ToolMakerTemplateMapper {
    String TABLE = "tool_maker_template";
    String SELECT_FIELDS = "id, created, status, type, name, agent_id, last_updated, user_id";
    String INSERT_FIELDS = "id, created, status, type, name, agent_id, last_updated, user_id";
    
    int insert(AIPortToolMakerTemplate template);
    
    int update(AIPortToolMakerTemplate template);
    
    int delete(@Param("id") long id);
    
    AIPortToolMakerTemplate selectById(@Param("id") long id);
    
    List<AIPortToolMakerTemplate> selectByUserId(@Param("userId") long userId);
    
    List<AIPortToolMakerTemplate> selectByAgentId(@Param("agentId") long agentId);
    
    List<AIPortToolMakerTemplate> selectByType(@Param("type") int type);
    
    List<AIPortToolMakerTemplate> selectAll();
    
    int count();
}