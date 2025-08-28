package ai.mcpdirect.backend.dao.mapper.aitool;
import ai.mcpdirect.backend.dao.entity.AIPortSystemProperty;
import ai.mcpdirect.backend.dao.mapper.AIPortSystemPropertyMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AIToolSystemPropertyMapper extends AIPortSystemPropertyMapper<AIPortSystemProperty> {
    String propertyTable = "aitool.aitool_property";
    String selectSystemProperty ="SELECT psp.\"key\", psp.archived, psp.created, psp.\"type\", psp.category, psp.value FROM "+propertyTable+" psp\n";
    // @Select(selectSystemProperty +"where psp.archived=0;")
    // List<AIPortSystemProperty> getSystemProperties();

    @Select(selectSystemProperty +"where psp.archived=0;")
    List<AIPortSystemProperty> getValidSystemProperties();

    @Select(selectSystemProperty +"where psp.archived=0 and psp.created>#{from};")
    List<AIPortSystemProperty> getValidSystemPropertiesFrom(long from);
}
