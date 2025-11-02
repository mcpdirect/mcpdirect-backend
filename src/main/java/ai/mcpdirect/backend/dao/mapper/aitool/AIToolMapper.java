package ai.mcpdirect.backend.dao.mapper.aitool;

import ai.mcpdirect.backend.dao.entity.aitool.AIPortTool;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AIToolMapper extends ToolProviderMapper, ToolAppMapper, ToolAgentMapper,
        MCPServerConfigMapper, ToolMakerMapper,ToolPermissionMapper,VirtualToolMapper,
        VirtualToolPermissionMapper, TeamToolMakerMapper,ToolMakerTemplateMapper {

    String TABLE_NAME = "aitool.tool";
    String SELECT_FIELDS = """
            id,user_id, maker_id makerId, status,
            last_updated lastUpdated, name,
            tags, hash,agent_id agentId,
            agent_status agentStatus,
            maker_status makerStatus, usage""";

    String TABLE_JOIN_NAME = "aitool.tool t";
    String SELECT_JOIN_FIELDS = """
            t.id,t.user_id,t.maker_id makerId,
            t.status,t.last_updated lastUpdated,
            t.name,t.tags,t.hash,
            t.agent_id agentId,
            t.agent_status agentStatus,
            t.maker_status makerStatus,usage""";

    @Insert("INSERT INTO " + TABLE_NAME + " (id,user_id, maker_id, status, last_updated, name, tags, hash, meta_data,agent_id,agent_status,maker_status) " +
            "VALUES (#{id},#{userId}, #{makerId}, #{status}, #{lastUpdated}, #{name}, #{tags}, #{hash}, #{metaData}," +
            "#{agentId}, #{agentStatus}, #{makerStatus})")
    int insertTool(AIPortTool tool);

    @Update("UPDATE " + TABLE_NAME + " SET last_updated = #{lastUpdated}, hash = #{hash}, meta_data = #{metaData} WHERE id = #{id}")
    int updateToolMetaData(AIPortTool tool);

    @Update("UPDATE " + TABLE_NAME + " SET agent_status = #{agentStatus} WHERE agent_id = #{agentId}")
    int updateToolAgentStatusOfTool(@Param("agentId") long agentId,@Param("agentStatus")int agentStatus);

    @Update("UPDATE " + TABLE_NAME + " SET agent_id=#{to} WHERE agent_id=#{from}")
    int transferTools(@Param("from")long from,@Param("to")long to);

    @Delete("DELETE FROM " + TABLE_NAME + " WHERE maker_id=#{makerId}")
    int deleteToolsByMakerId(long makerId);

    @Select("SELECT " + SELECT_FIELDS + ",meta_data metaData FROM " + TABLE_NAME + " WHERE id = #{id}")
    AIPortTool selectToolById(long id);
    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME + " WHERE maker_id=#{makerId} AND name = #{name}")
    AIPortTool selectToolByName(@Param("makerId")long makerId,@Param("name") String name);

    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME + " WHERE maker_id = #{makerId}")
    List<AIPortTool> selectToolsByMakerId(long makerId);

    @Select("<script>SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME+"\n" + """
            WHERE maker_id IN
            <foreach item='item' index='index' collection='makerIds' open='(' separator=', ' close=')'>
            #{item}
            </foreach></script>""")
    List<AIPortTool> selectToolsByMakerIds(@Param("makerIds") List<Long> makerIds);

    @Select("<script>SELECT " + SELECT_JOIN_FIELDS + " FROM " + TABLE_JOIN_NAME +
            " LEFT JOIN " + ToolAgentMapper.TABLE_JOIN_NAME + " ON t.agent_id=ta.id "+ """
            WHERE t.user_id=#{userId}
            <if test="status!=null">AND t.status=#{status}</if>
            <if test="agentId!=null">AND t.agent_id=#{agentId}</if>
            <if test="makerId!=null">AND t.maker_id=#{makerId}</if>
            <if test="name!=null">AND t.name=#{name}</if>
            </script>""")
    List<AIPortTool> selectTools(@Param("userId") long userId,
                                 @Param("status") Integer status,
                                 @Param("agentId")Long agentId,
                                 @Param("makerId")Long makerId,
                                 @Param("name")String name);
}