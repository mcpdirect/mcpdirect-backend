package ai.mcpdirect.backend.dao.mapper.aitool;

import ai.mcpdirect.backend.dao.entity.aitool.AIPortToolAccessKey;
import ai.mcpdirect.backend.dao.entity.aitool.AIPortToolAccessKeyCredential;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ToolAccessKeyMapper {
    String accessKeyTable="aitool.tool_access_key";
    String selectAccessKey = """
            SELECT id,
            status,"name",
            effective_date effectiveDate,
            expiration_date expirationDate,
            user_id userId,
            usage usage,
            created FROM
            """+accessKeyTable+"\n";
    String selectAccessKeyCredential = """
            SELECT id,
            secret_key secretKey,
            status,"name",
            effective_date effectiveDate,
            expiration_date expirationDate,
            user_id userId,
            usage usage,
            created FROM
            """+accessKeyTable+"\n";


//    @Select(selectAccessKeyCredential+"where user_id=#{userId} and id=#{accessKeyId}")
//    AIPortAccessKeyCredential selectAccessKeyCredentialById(@Param("userId")long userId,@Param("accessKeyId") int accessKeyId);

    @Select(selectAccessKeyCredential+"where id=#{accessKeyId}")
    AIPortToolAccessKeyCredential selectAccessKeyCredentialById(@Param("accessKeyId") long accessKeyId);


    @Select(selectAccessKey+"where secret_key=#{key}")
    AIPortToolAccessKey selectAccessKey(String key);
    @Select(selectAccessKey+" WHERE user_Id=#{userId} and id=#{id}")
    AIPortToolAccessKey selectAccessKeyById(@Param("userId")long userId,@Param("id") long id);

    @Select("<script>"+selectAccessKey+" WHERE user_Id=#{userId} AND id IN\n"+"""
                                <foreach item='item' index='index' collection='idList' open='(' separator=', ' close=')'>
                                #{item}
                                </foreach></script>""")
    List<AIPortToolAccessKey> selectAccessKeyByIds(@Param("userId")long userId, @Param("idList") List<Integer> idList);

    @Select(selectAccessKey+"where user_id=#{userId}")
    List<AIPortToolAccessKey> selectAccessKeyByUserId(long userId);

    @Insert("INSERT INTO "+accessKeyTable+
            "(id, secret_key,\"name\",status,effective_date, expiration_date, user_id,created)VALUES" +
            "(#{id},#{secretKey},#{name},#{status},#{effectiveDate},#{expirationDate},#{userId},#{created})")
    void insertAccessKeyCredential(AIPortToolAccessKeyCredential accessKey);

    @Update("UPDATE "+accessKeyTable+" SET name=#{name}, status=#{status} WHERE user_Id=#{userId} and id=#{id}>")
    int updateAccessKey(AIPortToolAccessKey accessKey);

    @Update("UPDATE "+accessKeyTable+" SET name=#{name} WHERE user_Id=#{userId} and id=#{id}")
    int updateAccessKeyName(AIPortToolAccessKey accessKey);

    @Update("UPDATE "+accessKeyTable+" SET status=#{status} WHERE user_Id=#{userId} and id=#{id}")
    int updateAccessKeyStatus(AIPortToolAccessKey accessKey);

    @Delete("DELETE FROM "+accessKeyTable+" WHERE user_Id=#{userId} and id=#{id}")
    int deleteAccessKey(@Param("userId")long userId,@Param("id") long id);

    @Update("UPDATE "+accessKeyTable+" SET user_id=#{to} WHERE user_Id=#{from}")
    int transferAccessKeys(@Param("from")long from,@Param("to")long to);
}
