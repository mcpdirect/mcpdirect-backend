package ai.mcpdirect.backend.util;

import com.fasterxml.jackson.annotation.JsonRawValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AIToolsDirectory {
    public static class Description{
        public String name;
//        @JsonRawValue
//        public String tags;
        @JsonRawValue
        public String metaData;
    }
    public static class Tools{
        public String engineId;
        public List<Description> descriptions;
    }
    public long userId;
    public Map<String,Tools> tools = new HashMap<>();

    public static AIToolsDirectory create(long userId){
        AIToolsDirectory provider = new AIToolsDirectory();
        provider.userId = userId;
        return provider;
    }
}