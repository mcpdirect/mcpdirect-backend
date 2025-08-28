package ai.mcpdirect.backend.dao.entity.aitool;

import com.fasterxml.jackson.annotation.JsonRawValue;

public class AIPortToolProviderWithToolsApp extends AIPortToolProvider {
    public String name;
    @JsonRawValue
    public String description;
    public String summary;
    public int rating;
}
