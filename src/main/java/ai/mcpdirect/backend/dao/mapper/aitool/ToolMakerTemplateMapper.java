package ai.mcpdirect.backend.dao.mapper.aitool;

import ai.mcpdirect.backend.dao.entity.aitool.AIPortToolMakerTemplate;
import ai.mcpdirect.backend.dao.entity.aitool.AIPortToolMakerTemplateInstance;
import ai.mcpdirect.backend.dao.mapper.account.TeamMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ToolMakerTemplateMapper {
    String TABLE = "aitool.tool_maker_template";
    String TABLE_JOIN = "aitool.tool_maker_template tmt";
    String SELECT_FIELDS = "id, created, status, type, name, agent_id agentId, last_updated lastUpdated," +
            "user_id userId,config,inputs";
    String SELECT_FIELDS_JOIN = "tmt.id, tmt.created, tmt.status, tmt.type, tmt.name, tmt.agent_id agentId," +
            "tmt.last_updated lastUpdated, tmt.user_id userId,tmt.config,tmt.inputs";
    String INSERT_FIELDS = "id, created, status, type, name, agent_id, last_updated, user_id,config,inputs";
    
    @Insert("INSERT INTO "+TABLE+"("+INSERT_FIELDS+") VALUES\n" +
            "(#{id}, #{created}, #{status}, #{type}, #{name}, #{agentId}, #{lastUpdated}, #{userId},#{config},#{inputs})")
    int insertToolMakerTemplate(AIPortToolMakerTemplate template);

    @Update("<script>UPDATE "+TABLE + "\n" + """
            SET last_updated=#{lastUpdated}
            <if test="name!=null">,name=#{name}</if>
            <if test="status!=null">,status=#{status}</if>
            <if test="config!=null">,config=#{config}</if>
            <if test="inputs!=null">,inputs=#{inputs}</if>
            WHERE id=#{id} AND user_id=#{userId}</script>""")
    int updateToolMakerTemplate(@Param("id")long id,
                                @Param("userId")long userId,
                                @Param("name")String name,
                                @Param("status")Integer status,
                                @Param("config")String config,@Param("inputs")String inputs,
                                @Param("lastUpdated")long lastUpdated);

    @Select("SELECT "+SELECT_FIELDS+" FROM "+TABLE+" WHERE id=#{id}")
    AIPortToolMakerTemplate selectToolMakerTemplateById(long id);

    @Select("SELECT "+SELECT_FIELDS+" FROM "+TABLE+" WHERE user_id=#{userId} AND last_updated>#{lastUpdated}")
    List<AIPortToolMakerTemplate> selectToolMakerTemplateByUserId(@Param("userId") long userId,@Param("lastUpdated") long lastUpdated);

    @Select("SELECT " + SELECT_FIELDS_JOIN +"\n" +
            "FROM "+ TeamMapper.teamMemberTable+" atm\n" +
            "JOIN "+TeamToolMakerTemplateMapper.TABLE+" ttmt on atm.team_id = ttmt.team_id\n" +
            "JOIN "+TABLE_JOIN+" on tmt.user_id<>#{memberId} AND ttmt.tool_maker_template_id = tmt.id and tmt.last_updated>#{lastUpdated}\n" +
            "WHERE atm.member_id=#{memberId}")
    List<AIPortToolMakerTemplate> selectToolMakerTemplateByMemberId(@Param("memberId") long memberId,@Param("lastUpdated") long lastUpdated);
}