package ai.mcpdirect.backend.dao.mapper.aitool;

import ai.mcpdirect.backend.dao.entity.aitool.AIPortToolMakerTeam;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ToolMakerTeamMapper {
    String TOOL_MAKER_TEAM_TABLE = "aitool.tool_maker_team";
    String SELECT_FROM_TOOL_MAKER_TEAM_TABLE =
            "SELECT tool_maker_id, team_id, status, created, last_updated FROM "+TOOL_MAKER_TEAM_TABLE+"\n";
    @Insert("INSERT INTO "+TOOL_MAKER_TEAM_TABLE+"""
            (tool_maker_id, team_id, status, created, last_updated)
            VALUES(#{toolMakerId}, #{teamId}, #{status}, #{created}, #{lastUpdated})""")
    void insertToolMakerTeam(AIPortToolMakerTeam t);

    @Select(SELECT_FROM_TOOL_MAKER_TEAM_TABLE+"WHERE team_id=#{team_Id}")
    List<AIPortToolMakerTeam> selectToolMakerTeamByTeamId(long teamId);

    @Update("UPDATE "+TOOL_MAKER_TEAM_TABLE+ """
            SET status=#{status},last_updated=#{lastUpdated}
            WHERE tool_maker_id=#{toolMakerId} AND team_id=#{teamId}
            """)
    int updateToolMakerTeam(AIPortToolMakerTeam t);
}
