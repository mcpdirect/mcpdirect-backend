package ai.mcpdirect.backend.dao;

import ai.mcpdirect.backend.dao.entity.AIPortSystemProperty;
import ai.mcpdirect.backend.dao.mapper.aitool.AIToolMapper;
import ai.mcpdirect.backend.dao.mapper.aitool.AIToolSystemPropertyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AIToolDataHelper extends DAOHelper<AIPortSystemProperty, AIToolSystemPropertyMapper>{
    protected static AIToolDataHelper INSTANCE;
    public static AIToolDataHelper getInstance(){
        return INSTANCE;
    }

    public AIToolDataHelper(){
        INSTANCE = this;
    }

    private AIToolMapper toolMapper;

    public AIToolMapper getAIToolMapper() {
        return toolMapper;
    }

    @Autowired
    public void setAIToolMapper(AIToolMapper toolMapper) {
        this.toolMapper = toolMapper;
    }
}