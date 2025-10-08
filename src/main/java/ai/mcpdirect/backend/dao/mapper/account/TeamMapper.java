package ai.mcpdirect.backend.dao.mapper.account;

import ai.mcpdirect.backend.dao.entity.account.AIPortTeam;
import ai.mcpdirect.backend.dao.entity.account.AIPortTeamMember;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface TeamMapper {
    String teamTable = "account.team";
    String teamMemberTable = "account.team_member";

    // Team queries
    String selectTeam = """
            SELECT id,
            name,
            created,
            owner_id ownerId,
            status FROM
            """ + teamTable + "\n";
    
    String selectTeamWithDetails = """
            SELECT t.id,
            t.name,
            t.created,
            t.owner_id ownerId,
            t.status FROM
            """ + teamTable + " t\n";

    // Team Member queries
    String selectTeamMember = """
            SELECT team_id teamId,
            member_id memberId,
            status,
            created,
            expiration_date expirationDate FROM
            """ + teamMemberTable + "\n";

    // Team operations
    @Select(selectTeam + "WHERE id = #{id}")
    AIPortTeam selectTeamById(@Param("id") long id);

    @Select(selectTeam + "WHERE owner_id = #{ownerId}")
    List<AIPortTeam> selectTeamsByOwnerId(@Param("ownerId") long ownerId);
    @Insert("INSERT INTO " + teamTable +
            "(id, name, created, owner_id,status) VALUES " +
            "(#{id}, #{name}, #{created}, #{ownerId}, #{status})")
    void insertTeam(AIPortTeam team);

    @Update("UPDATE " + teamTable + 
            " SET name = #{name} WHERE id = #{team.id}")
    int updateTeam(AIPortTeam team);

    @Delete("DELETE FROM " + teamTable + " WHERE id = #{id}")
    int deleteTeam(@Param("id") long id);

    @Delete("DELETE FROM " + teamTable + " WHERE owner_id = #{ownerId}")
    int deleteTeamsByOwnerId(@Param("ownerId") long ownerId);

    // Team Member operations
    @Select(selectTeamMember + "WHERE team_id = #{teamId}")
    List<AIPortTeamMember> selectTeamMembersByTeamId(@Param("teamId") long teamId);

    @Select(selectTeamMember + "WHERE member_id = #{memberId}")
    List<AIPortTeamMember> selectTeamMembersByMemberId(@Param("memberId") long memberId);

    @Insert("INSERT INTO " + teamMemberTable + 
            "(team_id, member_id, status, created, expiration_date) VALUES " +
            "(#{teamId}, #{memberId}, #{status}, #{created}, #{expirationDate})")
    void insertTeamMember(AIPortTeamMember teamMember);

    @Update("<script> UPDATE " + teamMemberTable + " SET " +
            "<trim suffixOverrides=\",\">" +
            "<if test='status != null'>status = #{status},</if>" +
            "<if test='expirationDate != null'>expiration_date = #{expirationDate},</if>" +
            "</trim> " +
            "WHERE team_id = #{teamMember.teamId} AND member_id = #{teamMember.memberId}</script>")
    int updateTeamMember(AIPortTeamMember teamMember);

    @Delete("DELETE FROM " + teamMemberTable + " WHERE team_id = #{teamId} AND member_id = #{memberId}")
    int deleteTeamMember(@Param("teamId") long teamId, @Param("memberId") long memberId);

    @Delete("DELETE FROM " + teamMemberTable + " WHERE team_id = #{teamId}")
    int deleteTeamMembersByTeamId(@Param("teamId") long teamId);

    @Delete("DELETE FROM " + teamMemberTable + " WHERE member_id = #{memberId}")
    int deleteTeamMembersByMemberId(@Param("memberId") long memberId);
}