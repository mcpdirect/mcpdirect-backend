package ai.mcpdirect.backend.dao.mapper.aitool;

import ai.mcpdirect.backend.dao.entity.aitool.AIPortMCPServerConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

//@Mapper
public interface MCPServerConfigMapper {

    String TABLE_NAME = "aitool.mcp_server_config";
    String TABLE_NAME_JOIN = "aitool.mcp_server_config msc";
    String SELECT_FIELDS = "id, transport,url, command, args, env,inputs";
    String SELECT_FIELDS_JOIN = " msc.id, msc.transport,msc.url, msc.command, msc.args, msc.env,msc.inputs";

    @Insert("INSERT INTO " + TABLE_NAME + " (id, transport,url, command, args, env,inputs) " +
            "VALUES (#{id},#{transport},#{url}, #{command}, #{args}, #{env},#{inputs})")
    void insertMCPServerConfig(AIPortMCPServerConfig mcpServerConfig);

    @Update("UPDATE " + TABLE_NAME + " SET url = #{url}, command = #{command}, args = #{args}, env = #{env} WHERE id = #{id}")
    int updateMCPServerConfig(AIPortMCPServerConfig mcpServerConfig);

//    @Select("SELECT " + SELECT_FIELDS_JOIN + " FROM " + TABLE_NAME_JOIN + "\n" +
//            "JOIN " +ToolMakerMapper.TABLE_JOIN_NAME+" ON tm.id=msc.id AND tm.template_id=0 AND tm.user_id=#{userId}\n" +
//            "WHERE msc.id = #{id}")
//    AIPortMCPServerConfig selectMCPServerConfigById(@Param("userId") long userId,@Param("id") long id);

    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME + " WHERE id = #{id}")
    AIPortMCPServerConfig selectMCPServerConfigById(@Param("id") long id);

    @Select("<script>SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME +"\n"+ """
            WHERE id IN
            <foreach item='item' index='index' collection='makerIds' open='(' separator=', ' close=')'>
            #{item}
            </foreach></script>""")
    List<AIPortMCPServerConfig> selectMCPServerConfigByMakerIds(@Param("makerIds") List<Long> makerIds);

}

    