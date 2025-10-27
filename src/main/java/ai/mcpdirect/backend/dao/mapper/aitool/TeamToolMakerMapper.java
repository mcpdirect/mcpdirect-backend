package ai.mcpdirect.backend.dao.mapper.aitool;

import ai.mcpdirect.backend.dao.entity.aitool.AIPortTeamToolMaker;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface TeamToolMakerMapper {
    String TEAM_TOOL_MAKER_TABLE = "aitool.team_tool_maker";
    String SELECT_FROM_TEAM_TOOL_MAKER_TABLE =
            "SELECT tool_maker_id toolMakerId, team_id teamId, status, created, last_updated lastUpdated FROM "+ TEAM_TOOL_MAKER_TABLE +"\n";
    @Insert("INSERT INTO "+ TEAM_TOOL_MAKER_TABLE +"""
            (tool_maker_id, team_id, status, created, last_updated)
            VALUES(#{toolMakerId}, #{teamId}, #{status}, #{created}, #{lastUpdated})""")
    void insertTeamToolMaker(AIPortTeamToolMaker t);

    @Select(SELECT_FROM_TEAM_TOOL_MAKER_TABLE +"WHERE team_id=#{team_Id}")
    List<AIPortTeamToolMaker> selectTeamToolMakerByTeamId(long teamId);

    @Update("UPDATE "+ TEAM_TOOL_MAKER_TABLE + "\n" +"""
            SET status=#{status},last_updated=#{lastUpdated}
            WHERE tool_maker_id=#{toolMakerId} AND team_id=#{teamId}
            """)
    int updateTeamToolMaker(AIPortTeamToolMaker t);
}
