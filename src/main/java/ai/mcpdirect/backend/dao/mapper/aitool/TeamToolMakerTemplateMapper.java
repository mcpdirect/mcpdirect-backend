package ai.mcpdirect.backend.dao.mapper.aitool;

import ai.mcpdirect.backend.dao.entity.aitool.AIPortTeamToolMakerTemplate;
import ai.mcpdirect.backend.dao.mapper.account.TeamMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface TeamToolMakerTemplateMapper {
    String TABLE = "aitool.team_tool_maker_template";
    String SELECT_FIELDS =
            "tool_maker_template_id toolMakerTemplateId, team_id teamId, status, created, last_updated lastUpdated";

    String TABLE_JOIN = "aitool.team_tool_maker_template ttmt";
    String SELECT_FIELDS_JOIN =
            "ttmt.tool_maker_template_id toolMakerTemplateId, ttmt.team_id teamId, ttmt.status, ttmt.created, ttmt.last_updated lastUpdated";
    @Insert("INSERT INTO "+ TABLE +"""
            (tool_maker_template_id, team_id, status, created, last_updated)
            VALUES(#{toolMakerTemplateId}, #{teamId}, #{status}, #{created}, #{lastUpdated})""")
    void insertTeamToolMakerTemplate(AIPortTeamToolMakerTemplate t);

    @Select("SELECT "+SELECT_FIELDS +" FROM "+TABLE+" WHERE team_id=#{teamId} and last_updated>#{lastUpdated}")
    List<AIPortTeamToolMakerTemplate> selectTeamToolMakerTemplatesByTeamId(
            @Param("teamId") long teamId,@Param("lastUpdated") long lastUpdated
    );

    @Select("SELECT "+SELECT_FIELDS_JOIN +" FROM "+TABLE+" ttmt\n" +
            "LEFT JOIN "+ TeamMapper.teamMemberTable + " tm on tm.team_id = ttmt.team_id and tm.member_id = #{memberId}\n"+
            "WHERE ttmt.last_updated>#{lastUpdated}")
    List<AIPortTeamToolMakerTemplate> selectTeamToolMakerTemplatesByMemberId(
            @Param("memberId") long memberId,@Param("lastUpdated") long lastUpdated
    );

    @Update("UPDATE "+ TABLE + "\n" +"""
            SET status=#{status},last_updated=#{lastUpdated}
            WHERE tool_maker_template_id=#{toolMakerTemplateId} AND team_id=#{teamId}
            """)
    int updateTeamToolMakerTemplate(AIPortTeamToolMakerTemplate t);
}
