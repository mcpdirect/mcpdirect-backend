package ai.mcpdirect.backend.dao.mapper.aitool;

import ai.mcpdirect.backend.dao.entity.aitool.AIPortVirtualTool;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface VirtualToolMapper {
    String TABLE_NAME = "aitool.virtual_tool";
    String TABLE_JOIN_NAME = "aitool.virtual_tool vt";
    String SELECT_JOIN_FIELDS = """
            vt.id,vt.maker_id makerId,vt.status,
            vt.tool_id toolId,vt.last_updated lastUpdated,
            vt.maker_status makerStatus""";

    @Insert("INSERT INTO "+TABLE_NAME + "(id, maker_id, tool_id, status, tags, maker_status, last_updated)\n" +
            "VALUES(#{id}, #{makerId}, #{toolId}, #{status}, #{tags}, #{makerStatus}, #{lastUpdated})")
    void insertVirtualTool(AIPortVirtualTool tool);

    @Update("UPDATE "+TABLE_NAME+" SET status=#{status},last_updated=#{lastUpdated}" +
            " WHERE id=#{id} AND maker_id=#{makerId}")
    int updateVirtualToolStatus(AIPortVirtualTool tool);

    @Select("SELECT "+SELECT_JOIN_FIELDS+",CONCAT(vt.tags,',',t.tags) tags," +
            "t.name,t.agent_id agentId," +
            "t.agent_status agentStatus FROM "+TABLE_JOIN_NAME+
            " LEFT JOIN "+AIToolMapper.TABLE_JOIN_NAME+" ON t.id=vt.tool_id\n"+
            " WHERE vt.status>-1 AND vt.maker_id=#{makerId}")
    List<AIPortVirtualTool> selectVirtualToolByMakerId(@Param("makerId")long makerId);
    @Select("SELECT "+SELECT_JOIN_FIELDS+",CONCAT(vt.tags,',',t.tags) tags," +
            "t.name,t.agent_id agentId," +
            "t.agent_status agentStatus FROM "+TABLE_JOIN_NAME+
            " LEFT JOIN "+ToolMakerMapper.TABLE_JOIN_NAME+" ON tm.id=vt.maker_id\n"+
            " LEFT JOIN "+AIToolMapper.TABLE_JOIN_NAME+" ON t.id=vt.tool_id\n"+
            " WHERE vt.status>-1 AND tm.agent_id=#{userId}")
    List<AIPortVirtualTool> selectVirtualTools(@Param("userId")long userId);
}
