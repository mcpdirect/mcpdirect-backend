package ai.mcpdirect.backend.dao.mapper.aitool;

import ai.mcpdirect.backend.dao.entity.aitool.AIPortToolAgent;
import org.apache.ibatis.annotations.*;

import java.util.List;

//@Mapper
public interface ToolAgentMapper {

    String TABLE_NAME = "aitool.tool_agent";
    String SELECT_FIELDS = "id, user_id userId, engine_id engineId, created, device, name, tags, status,device_id";

    String TABLE_JOIN_NAME = "aitool.tool_agent ta";

    @Insert("INSERT INTO " + TABLE_NAME + " (id, user_id, engine_id, created, device, name, tags, status,device_id) " +
            "VALUES (#{id}, #{userId}, #{engineId}, #{created}, #{device}, #{name}, #{tags}, #{status},#{deviceId})")
    int insertToolAgent(AIPortToolAgent toolAgent);

    @Update("UPDATE " + TABLE_NAME + " SET created = #{created}, device = #{device}, name = #{name}, tags = #{tags}, status = #{status} WHERE id = #{id}")
    int updateToolAgent(AIPortToolAgent toolAgent);

    @Update("UPDATE " + TABLE_NAME + " SET status = #{status} WHERE id = #{id}")
    int updateToolAgentStatus(AIPortToolAgent toolAgent);

    @Update("UPDATE " + TABLE_NAME + " SET name = #{name} WHERE id = #{id}")
    int updateToolAgentName(AIPortToolAgent toolAgent);

    @Update("UPDATE " + TABLE_NAME + " SET tags = #{tags} WHERE id = #{id}")
    int updateToolAgenTags(AIPortToolAgent toolAgent);

//    @Update("UPDATE " + TABLE_NAME + " SET user_id=#{to} WHERE user_id=#{from}")
//    int transferToolAgents(@Param("from")long from,@Param("to")long to);

    @Update("UPDATE " + TABLE_NAME + " SET user_id=#{to} WHERE user_id=#{from} AND id=#{id}")
    int transferToolAgent(@Param("from")long from,@Param("to")long to,@Param("id") long id);

    @Delete("DELETE FROM "+TABLE_NAME+" WHERE user_id=#{userId}")
    void deleteToolAgentsByUserId(long userId);

    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME + " WHERE user_id = #{userId}")
    List<AIPortToolAgent> selectToolAgentsByUserId(long userId);
    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME + " WHERE id = #{id}")
    AIPortToolAgent selectToolAgentById(long id);

    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME + " WHERE user_id = #{userId} and engine_id=#{engineId}")
    AIPortToolAgent selectToolAgentByEngineId(@Param("userId") long userId, @Param("engineId") String engineId);

//    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME + " WHERE user_id = #{userId}")
//    List<AIPortToolAgent> selectToolAgentByUserId(long userId);

    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME + " WHERE user_id = #{userId} AND engine_id = #{engineId}")
    AIPortToolAgent selectToolAgentByUserIdAndEngineId(@Param("userId") long userId, @Param("engineId") long engineId);

    @Select("<script>SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME +"\n"+ """
                                WHERE id IN
                                <foreach item='item' index='index' collection='agentIds' open='(' separator=', ' close=')'>
                                #{item}
                                </foreach></script>""")
    List<AIPortToolAgent> selectToolAgentByIds(@Param("agentIds") List<Long> agentIds);

}