package ai.mcpdirect.backend.dao.entity.aitool;

public class AIPortToolMaker extends AIPortToolMakerStub{
    public int status;
    public long lastUpdated;
    public String tags;
    public long templateId;

    public AIPortToolMaker name(String name){
        if(name==null||(name=name.trim()).isEmpty()||name.length()>50){
            throw new RuntimeException("invalid name");
        }
        this.name = name;
        return this;
    }
    public AIPortToolMaker tags(String tags){
        if(tags!=null&&(tags=tags.trim()).length()>100){
            throw new RuntimeException("invalid tags");
        }
        this.tags = tags;
        return this;
    }
}
