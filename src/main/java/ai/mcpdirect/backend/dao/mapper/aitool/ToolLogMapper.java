package ai.mcpdirect.backend.dao.mapper.aitool;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface ToolLogMapper {
    String TABLE_NAME = "aitool.tool_log";
    String SELECT_FIELDS = "user_id userId,key_id keyId,tool_id toolId,created";

    @Insert("INSERT INTO "+TABLE_NAME+" (user_id,key_id,tool_id,created)\n" +
            "VALUES(#{userId},#{keyId},#{toolId},#{created})")
    void insertToolLog(@Param("userId") long userId,@Param("keyId") long keyId,@Param("toolId") long toolId,@Param("created") long created);
}
