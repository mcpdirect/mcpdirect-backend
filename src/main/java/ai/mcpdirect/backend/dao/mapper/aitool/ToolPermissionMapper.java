package ai.mcpdirect.backend.dao.mapper.aitool;

import ai.mcpdirect.backend.dao.entity.aitool.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

//@Mapper
public interface ToolPermissionMapper {

    String TABLE_NAME = "aitool.tool_permission";
    String TABLE_JOIN_NAME = "aitool.tool_permission tp";
    String SELECT_FIELDS = "user_id userId, access_key_id accessKeyId, tool_id toolId, last_updated lastUpdated, status";
    String SELECT_JOIN_FIELDS = "tp.user_id userId, tp.access_key_id accessKeyId, tp.tool_id toolId, tp.last_updated lastUpdated, tp.status";

    String TABLE_NAME_V = "aitool.virtual_tool_permission";
    String TABLE_JOIN_NAME_V = "aitool.virtual_tool_permission tp";
    String SELECT_FIELDS_V = "user_id userId, access_key_id accessKeyId, tool_id toolId, original_tool_id originalToolId, last_updated lastUpdated, status";
    String SELECT_JOIN_FIELDS_V = "tp.user_id userId, tp.access_key_id accessKeyId, tp.tool_id toolId,tp.original_tool_id originalToolId, tp.last_updated lastUpdated, tp.status";

    String SELECT_TOOL_FIELDS = "t.id,t.name,t.agent_id agentId,t.maker_id makerId,t.tags,t.meta_data metaData";

    @Insert("INSERT INTO " + TABLE_NAME + " (user_id, access_key_id, tool_id, last_updated, status) " +
            "VALUES (#{userId}, #{accessKeyId}, #{toolId}, #{lastUpdated}, #{status})")
    void insertToolPermission(AIPortToolPermission permission);

    @Insert("INSERT INTO " + TABLE_NAME_V + " (user_id, access_key_id, tool_id,original_tool_id, last_updated, status) " +
            "VALUES (#{userId}, #{accessKeyId}, #{toolId},#{originalToolId}, #{lastUpdated}, #{status})")
    int insertVirtualToolPermission(AIPortVirtualToolPermission permission);

    @Update("UPDATE " + TABLE_NAME + " SET last_updated = #{lastUpdated}, status = #{status}\n" +
            "WHERE user_id = #{userId} AND access_key_id = #{accessKeyId} AND tool_id = #{toolId}")
    int updateToolPermission(AIPortToolPermission permission);

    @Update("UPDATE " + TABLE_NAME_V + " SET last_updated = #{lastUpdated}, status = #{status}\n" +
            "WHERE user_id = #{userId} AND access_key_id = #{accessKeyId} AND tool_id = #{toolId}")
    int updateVirtualToolPermission(AIPortVirtualToolPermission permission);

    @Update("UPDATE " + TABLE_NAME + " SET user_id = #{to} WHERE user_id = #{from}")
    int transferToolPermissions(@Param("from")long from,@Param("to")long to);

    @Delete("<script>DELETE FROM " + TABLE_NAME + """
            WHERE tool_id IN
            <foreach item='item' index='index' collection='toolIdLists' open='(' separator=', ' close=')'>
            #{item}
            </foreach></script>
            """)
    int deleteToolPermissionsByIdList(@Param("toolIdList")List<Long> idList);

    @Delete("<script>DELETE FROM " + TABLE_NAME_V + """
            WHERE tool_id IN
            <foreach item='item' index='index' collection='toolIdLists' open='(' separator=', ' close=')'>
            #{item}
            </foreach></script>
            """)
    int deleteVirtualToolPermissionsByIdList(@Param("toolIdList")List<Long> idList);

    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME + " WHERE user_id = #{userId} AND access_key_id = #{accessKeyId} AND tool_id = #{toolId}")
    AIPortToolPermission selectToolPermission(@Param("userId") long userId, @Param("accessKeyId") int accessKeyId, @Param("toolId") long toolId);

    @Select("SELECT " + SELECT_JOIN_FIELDS + ",t.name,t.agent_id agentId,t.maker_id makerId FROM " + TABLE_JOIN_NAME +"\n"+
            "LEFT JOIN "+AIToolMapper.TABLE_JOIN_NAME+" ON tp.tool_id = t.id\n" +
            " WHERE tp.user_id = #{userId} AND tp.access_key_id = #{accessKeyId}")
    List<AIPortToolPermission> selectToolPermissionByAccessKey(@Param("userId") long userId, @Param("accessKeyId") long accessKeyId);

    @Select("SELECT " + SELECT_JOIN_FIELDS_V + ",t.name,vt.maker_id makerId FROM " + TABLE_JOIN_NAME_V +"\n"+
            "LEFT JOIN "+AIToolMapper.TABLE_JOIN_NAME+" ON tp.original_tool_id = t.id\n" +
            "LEFT JOIN "+VirtualToolMapper.TABLE_JOIN_NAME+" ON tp.tool_id = vt.id\n" +
            " WHERE tp.user_id = #{userId} AND tp.access_key_id = #{accessKeyId}")
    List<AIPortVirtualToolPermission> selectVirtualToolPermissionByAccessKey(@Param("userId") long userId, @Param("accessKeyId") long accessKeyId);

    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME + " WHERE user_id = #{userId}")
    List<AIPortToolPermission> selectToolPermissionsByUserId(@Param("userId") long userId);

    @Select("SELECT " + SELECT_FIELDS_V + " FROM " + TABLE_NAME_V + " WHERE user_id = #{userId}")
    List<AIPortVirtualToolPermission> selectVirtualToolPermissionsByUserId(@Param("userId") long userId);

    @Select("SELECT "+SELECT_TOOL_FIELDS+" FROM "+TABLE_NAME+" tp\n" +
            "LEFT JOIN "+AIToolMapper.TABLE_NAME+" t ON tp.tool_id = t.id\n" +
            "WHERE tp.access_key_id=#{accessKeyId} AND tp.status=1\n" +
            "AND t.status=1 AND t.agent_status=1 AND t.maker_status=1\n" +
            "ORDER BY t.agent_id")
    List<AIPortTool> selectPermittedTools(@Param("accessKeyId") long accessKeyId);

    @Select("SELECT "+SELECT_TOOL_FIELDS+" FROM "+TABLE_NAME_V+" tp\n" +
            "LEFT JOIN "+AIToolMapper.TABLE_NAME+" t ON tp.original_tool_id = t.id\n" +
            "WHERE tp.access_key_id=#{accessKeyId} AND tp.status=1\n" +
            "AND t.status=1 AND t.agent_status=1 AND t.maker_status=1\n" +
            "ORDER BY t.agent_id")
    List<AIPortTool> selectVirtualPermittedTools(@Param("accessKeyId") long accessKeyId);

    @Select("""
            select ak.id accessKeyId,t.maker_id makerId,count(t.maker_id)  from account.access_key ak
            left join aitool.tool_permission tp on tp.access_key_id = ak.id
            left join aitool.tool t on t.id=tp.tool_id
            where tp.status > 0 and
            """+ "ak.user_id = #{userId} group by ak.id,t.maker_id")
    List<AIPortToolPermissionMakerSummary> selectToolPermissionMakerSummary(long userId);
    @Select("""
            select ak.id accessKeyId,t.maker_id makerId,count(t.maker_id)  from account.access_key ak
            left join aitool.virtual_tool_permission tp on tp.access_key_id = ak.id
            left join aitool.virtual_tool t on t.id=tp.tool_id
            where tp.status > 0 and
            """+ "ak.user_id = #{userId} group by ak.id,t.maker_id")
    List<AIPortToolPermissionMakerSummary> selectVirtualToolPermissionMakerSummary(long userId);
}
