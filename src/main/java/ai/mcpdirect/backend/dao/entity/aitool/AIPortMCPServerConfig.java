package ai.mcpdirect.backend.dao.entity.aitool;

import ai.mcpdirect.backend.util.ID;

public class AIPortMCPServerConfig {
    public long id;
    
    public long created;
    public int type;
    public String url;
    public String command;
    public String args;
    public String env;
}