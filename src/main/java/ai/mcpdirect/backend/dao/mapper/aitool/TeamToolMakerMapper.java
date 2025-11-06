package ai.mcpdirect.backend.dao.mapper.aitool;

import ai.mcpdirect.backend.dao.entity.aitool.AIPortTeamToolMaker;
import ai.mcpdirect.backend.dao.mapper.account.TeamMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface TeamToolMakerMapper {
    String TEAM_TOOL_MAKER_TABLE = "aitool.team_tool_maker";
    String SELECT_FROM_TEAM_TOOL_MAKER_TABLE =
            "SELECT tool_maker_id toolMakerId, team_id teamId, status, created, last_updated lastUpdated FROM "+ TEAM_TOOL_MAKER_TABLE +"\n";
    String SELECT_FIELDS_JOIN = "ttm.tool_maker_id toolMakerId, ttm.team_id teamId, ttm.status, ttm.created, ttm.last_updated lastUpdated";
    @Insert("INSERT INTO "+ TEAM_TOOL_MAKER_TABLE +"""
            (tool_maker_id, team_id, status, created, last_updated)
            VALUES(#{toolMakerId}, #{teamId}, #{status}, #{created}, #{lastUpdated})""")
    void insertTeamToolMaker(AIPortTeamToolMaker t);

    @Select(SELECT_FROM_TEAM_TOOL_MAKER_TABLE +"WHERE team_id=#{teamId}")
    List<AIPortTeamToolMaker> selectTeamToolMakerByTeamId(long teamId);

    @Select("SELECT "+SELECT_FIELDS_JOIN +" FROM "+TEAM_TOOL_MAKER_TABLE+" ttm\n" +
            "LEFT JOIN "+ TeamMapper.teamMemberTable + " tm on tm.team_id = ttm.team_id and tm.member_id = #{memberId}\n"+
            "WHERE ttm.last_updated>#{lastUpdated}")
    List<AIPortTeamToolMaker> selectTeamToolMakersByMemberId(
            @Param("memberId") long memberId, @Param("lastUpdated") long lastUpdated
    );

    @Update("UPDATE "+ TEAM_TOOL_MAKER_TABLE + "\n" +"""
            SET status=#{status},last_updated=#{lastUpdated}
            WHERE tool_maker_id=#{toolMakerId} AND team_id=#{teamId}
            """)
    int updateTeamToolMaker(AIPortTeamToolMaker t);
}
