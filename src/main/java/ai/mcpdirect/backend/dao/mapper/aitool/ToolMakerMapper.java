package ai.mcpdirect.backend.dao.mapper.aitool;

import ai.mcpdirect.backend.dao.entity.aitool.AIPortToolMaker;
import org.apache.ibatis.annotations.*;

import java.util.List;

//@Mapper
public interface ToolMakerMapper {

    String TABLE_NAME = "aitool.tool_maker";
//    String SELECT_FIELDS = "id, created, status, last_updated lastUpdated, hash, tools, type, name, tags, agent_id agentId";
    String SELECT_FIELDS = "id, created, status, last_updated lastUpdated,type, name, tags, agent_id agentId";
    String SELECT_JOIN_FIELDS = "tm.id, tm.created, tm.status, tm.last_updated lastUpdated,tm.type, tm.name, tm.tags, tm.agent_id agentId";

//    @Insert("INSERT INTO " + TABLE_NAME + " (id, created, status, last_updated, hash, tools, type, name, tags, agent_id) " +
//            "VALUES (#{id}, #{created}, #{status}, #{lastUpdated}, #{hash}, #{tools}, #{type}, #{name}, #{tags}, #{agentId})")
    @Insert("INSERT INTO " + TABLE_NAME + " (id, created, status, last_updated, type, name, tags, agent_id) " +
            "VALUES (#{id}, #{created}, #{status}, #{lastUpdated}, #{type}, #{name}, #{tags}, #{agentId})")

    void insertToolMaker(AIPortToolMaker toolsMaker);

    @Update("UPDATE " + TABLE_NAME + " SET status = #{status} WHERE id = #{id}")
    int updateToolMakerStatus(@Param("id")long id,@Param("status")int status);

    @Delete("DELETE FROM " + TABLE_NAME + " WHERE id = #{id}")
    int deleteToolMaker(@Param("id")long id);


    @Update("UPDATE " + TABLE_NAME + " SET agent_id = #{to},name=CONCAT(name,'-','#{datetime}') WHERE agent_id = #{from}")
    int transferToolMakers(@Param("from")long from,@Param("to")long to,@Param("datetime")String datetime);

//    @Update("UPDATE " + TABLE_NAME + " SET last_updated = #{lastUpdated}, hash = #{hash}, tools = #{tools} WHERE id = #{id}")
//    @Update("UPDATE " + TABLE_NAME + " SET last_updated = #{lastUpdated}, hash = #{hash}, tools = #{tools} WHERE id = #{id}")
//    int updateToolsMakerTools(@Param("id")long id,@Param("lastUpdated")long lastUpdate,
//                              @Param("hash")long hash,@Param("tools")String tools);
    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME + " WHERE id = #{id}")
    AIPortToolMaker selectToolMakerById(@Param("id") long id);

//    @Select("SELECT " + SELECT_JOIN_FIELDS+" , ta.status agentStatus" + " FROM " + TABLE_NAME + " tm\n" +
//            "LEFT JOIN "+ToolsAgentMapper.TABLE_NAME +" ta on tm.agent_id = ta.id\n"+
//            "WHERE tm.id = #{id}")
//    AIPortToolsMakerWithAgent selectToolsMakerWithAgentById(@Param("id") long id);

//    @Select("SELECT " + SELECT_JOIN_FIELDS+" , ta.status agentStatus" + " FROM " + TABLE_NAME + " tm\n" +
//            "LEFT JOIN "+ToolsAgentMapper.TABLE_NAME +" ta on tm.agent_id = ta.id\n"+
//            " WHERE tm.agent_id = #{agentId} and tm.name=#{name}")
//    AIPortToolsMakerWithAgent selectToolsMakerWithAgentByName(@Param("agentId") long agentId,@Param("name")String name);

    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME + " WHERE agent_id = #{agentId} and name=#{name}")
    AIPortToolMaker selectToolMakerByName(@Param("agentId") long agentId, @Param("name")String name);

    @Select("SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME + " WHERE agent_id = #{agentId}")
    List<AIPortToolMaker> selectToolMakerByAgentId(@Param("agentId") long agentId);

    @Select("<script>SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME +"\n"+ """
                                WHERE agent_id IN
                                <foreach item='item' index='index' collection='agentIds' open='(' separator=', ' close=')'>
                                #{item}
                                </foreach></script>""")
    List<AIPortToolMaker> selectToolMakerByAgentIds(@Param("agentIds") List<Long> agentIds);

    @Select("<script>SELECT " + SELECT_FIELDS + " FROM " + TABLE_NAME +"\n"+ """
                                WHERE id IN
                                <foreach item='item' index='index' collection='makerIds' open='(' separator=', ' close=')'>
                                #{item}
                                </foreach></script>""")
    List<AIPortToolMaker> selectToolMakerByIds(@Param("makerIds") List<Long> makerIds);
}
