package ai.mcpdirect.backend.dao.mapper.aitool;

import ai.mcpdirect.backend.dao.entity.aitool.AIPortTeamToolMakerTemplate;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface TeamToolMakerTemplateMapper {
    String TABLE = "aitool.team_tool_maker_template";
    String SELECT_FIELDS =
            "tool_maker_template_id toolMakerTemplateId, team_id teamId, status, created, last_updated lastUpdated";
    @Insert("INSERT INTO "+ TABLE +"""
            (tool_maker_template_id, team_id, status, created, last_updated)
            VALUES(#{toolMakerTemplateId}, #{teamId}, #{status}, #{created}, #{lastUpdated})""")
    void insertTeamToolMakerTemplate(AIPortTeamToolMakerTemplate t);

    @Select("SELECT "+SELECT_FIELDS +" FROM "+TABLE+" WHERE team_id=#{team_Id}")
    List<AIPortTeamToolMakerTemplate> selectTeamToolMakerTemplatesByTeamId(long teamId);

    @Update("UPDATE "+ TABLE + "\n" +"""
            SET status=#{status},last_updated=#{lastUpdated}
            WHERE tool_maker_template_id=#{toolMakerTemplateId} AND team_id=#{teamId}
            """)
    int updateTeamToolMakerTemplate(AIPortTeamToolMakerTemplate t);
}
