package ai.mcpdirect.backend.dao.entity.aitool;

import ai.mcpdirect.backend.util.ID;
import com.fasterxml.jackson.annotation.JsonRawValue;

public class AIPortToolProvider {
    public long id;
    public long userId;
    /**
     * relate to db field provider_id
     */
    public String providerId;
    @JsonRawValue
    public String tools;
    /**
     * relate to db field last_updated
     */
    public long lastUpdated;
    public long hash;
    /**
     * relate to db field app_id
     */
    public long appId;
    public String device;
    public AIPortToolProvider(){}
    public AIPortToolProvider(long userId, String providerId, String tools, long hash, long lastUpdated, long appId, String device) {
        this.id = ID.nextId();
        this.userId = userId;
        this.providerId = providerId;
        this.tools = tools;
        this.lastUpdated = lastUpdated;
        this.hash = hash;
        this.appId = appId;
        this.device = device;
    }
}
