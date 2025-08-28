package ai.mcpdirect.backend.dao.mapper.aitool;

import ai.mcpdirect.backend.dao.entity.aitool.AIPortMCPServerConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

//@Mapper
public interface MCPServerConfigMapper {

    String TABLE_NAME = "aitool.mcp_server_config";
    String SELECT_FIELDS = "id, created, url, command, args, env";

    @Insert("INSERT INTO " + TABLE_NAME + " (id, created, url, command, args, env) " +
            "VALUES (#{id}, #{created}, #{url}, #{command}, #{args}, #{env})")
    int insertMCPServerConfig(AIPortMCPServerConfig mcpServerConfig);

    @Update("UPDATE " + TABLE_NAME + " SET created = #{created}, url = #{url}, command = #{command}, args = #{args}, env = #{env} WHERE id = #{id}")
    int updateMCPServerConfig(AIPortMCPServerConfig mcpServerConfig);

    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME + " WHERE id = #{id}")
    AIPortMCPServerConfig selectMCPServerConfigById(long id);

    @Select("<script>SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME +"\n"+ """
            WHERE id IN
            <foreach item='item' index='index' collection='makerIds' open='(' separator=', ' close=')'>
            #{item}
            </foreach></script>""")
    List<AIPortMCPServerConfig> selectMCPServerConfigByMakerIds(@Param("makerIds") List<Long> makerIds);

}

    