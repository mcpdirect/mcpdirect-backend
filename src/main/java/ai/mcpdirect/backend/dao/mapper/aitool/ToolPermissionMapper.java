package ai.mcpdirect.backend.dao.mapper.aitool;

import ai.mcpdirect.backend.dao.entity.aitool.AIPortTool;
import ai.mcpdirect.backend.dao.entity.aitool.AIPortToolPermission;
import org.apache.ibatis.annotations.*;

import java.util.List;

//@Mapper
public interface ToolPermissionMapper {

    String TABLE_NAME = "aitool.tool_permission";
    String SELECT_FIELDS = "user_id userId, access_key_id accessKeyId, tool_id toolId, last_updated lastUpdated, status";

    String SELECT_TOOL_FIELDS = "t.name,t.agent_id agentId,t.maker_id makerId,t.tags,t.meta_data metaData";

    @Insert("INSERT INTO " + TABLE_NAME + " (user_id, access_key_id, tool_id, last_updated, status) " +
            "VALUES (#{userId}, #{accessKeyId}, #{toolId}, #{lastUpdated}, #{status})")
    int insertToolPermission(AIPortToolPermission permission);

    @Update("UPDATE " + TABLE_NAME + " SET last_updated = #{lastUpdated}, status = #{status}\n" +
            "WHERE user_id = #{userId} AND access_key_id = #{accessKeyId} AND tool_id = #{toolId}")
    int updateToolPermission(AIPortToolPermission permission);

    @Update("UPDATE " + TABLE_NAME + " SET last_updated = #{lastUpdated}, status = #{status}\n" +
            "WHERE user_id = #{userId} AND access_key_id = #{accessKeyId}")
    int updateToolPermissions(AIPortToolPermission permission);

    @Update("UPDATE " + TABLE_NAME + " SET user_id = #{to} WHERE user_id = #{from}")
    int transferToolPermissions(@Param("from")long from,@Param("to")long to);

    @Delete("<script>DELETE FROM " + TABLE_NAME + """
            WHERE tool_id IN
            <foreach item='item' index='index' collection='toolIdLists' open='(' separator=', ' close=')'>
            #{item}
            </foreach></script>
            """)
    int deleteToolPermissionsByIdList(@Param("toolIdList")List<Long> idList);

    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME + " WHERE user_id = #{userId} AND access_key_id = #{accessKeyId} AND tool_id = #{toolId}")
    AIPortToolPermission selectToolPermission(@Param("userId") long userId, @Param("accessKeyId") int accessKeyId, @Param("toolId") long toolId);

    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME + " WHERE user_id = #{userId} AND access_key_id = #{accessKeyId}")
    List<AIPortToolPermission> selectToolPermissionByAccessKey(@Param("userId") long userId, @Param("accessKeyId") int accessKeyId);

    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME + " WHERE user_id = #{userId}")
    List<AIPortToolPermission> selectToolPermissionsByUserId(@Param("userId") long userId);

    @Select("SELECT "+SELECT_TOOL_FIELDS+" FROM "+TABLE_NAME+" tp\n" +
            "LEFT JOIN "+AIToolMapper.TABLE_NAME+" t ON tp.tool_id = t.id\n" +
            "WHERE tp.access_key_id=#{accessKeyId} AND tp.status=1\n" +
            "AND t.status=1 AND t.agent_status=1 AND t.maker_status=1\n" +
            "ORDER BY t.agent_id")
    List<AIPortTool> selectPermittedTools(@Param("accessKeyId") long accessKeyId);
}
