package ai.mcpdirect.backend.dao.mapper.account;
import ai.mcpdirect.backend.dao.entity.AIPortSystemProperty;
import ai.mcpdirect.backend.dao.mapper.AIPortSystemPropertyMapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AccountSystemPropertyMapper extends AIPortSystemPropertyMapper<AIPortSystemProperty> {
    String propertyTable = "account.account_property";
    String selectSystemProperty ="SELECT psp.\"key\", psp.archived, psp.created, psp.\"type\", psp.category, psp.value FROM "+propertyTable+" psp\n";
    // @Select(selectSystemProperty +"where psp.archived=0;")
    // List<AIPortSystemProperty> getSystemProperties();

    @Select(selectSystemProperty +"where psp.archived=0;")
    List<AIPortSystemProperty> getValidSystemProperties();

    @Select(selectSystemProperty +"where psp.archived=0 and psp.created>#{from};")
    List<AIPortSystemProperty> getValidSystemPropertiesFrom(long from);

    @Select("<script>"+selectSystemProperty + """
            where psp.archived
            <if test="archived==true">&gt;</if>
            <if test="archived==false">=</if> 0
            <if test="key!=null">and psp.key like CONCAT('%', #{key}, '%')</if></script>""")
    List<AIPortSystemProperty> getSystemProperties(@Param("key")String key,@Param("archived")boolean archived);

    @Select(selectSystemProperty +"where psp.key=#{key} and psp.archived=0;")
    AIPortSystemProperty getSystemProperty(@Param("key") String key,@Param("archived")long archived);

    @Insert("insert into\n"+propertyTable+"\n(key,archived,created,type,category,value) " +
            "values(#{key}, #{archived}, #{created}, #{type}, #{category}, #{value})")
    void insertSystemProperty(AIPortSystemProperty property);
    @Update("update\n"+propertyTable+"\nset type=#{type},category=#{category},value=#{value} where key=#{key} and archived=0")
    void updateSystemProperty(AIPortSystemProperty property);

    @Update("update\n"+propertyTable+"\nset archived=#{archived} where key=#{key} and archived=#{lastArchived}")
    void updateSystemPropertyArchived(@Param("key") String key,@Param("archived") long archived,@Param("lastArchived") long lastArchived);

}
