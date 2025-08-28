package ai.mcpdirect.backend.dao.mapper.account;

import ai.mcpdirect.backend.dao.entity.account.AIPortAccessKey;

import ai.mcpdirect.backend.dao.entity.account.AIPortAccessKeyCredential;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface AccessKeyMapper {
    String accessKeyTable="account.access_key";
    String selectAccessKey = """
            SELECT id,
            status,"name",
            effective_date effectiveDate,
            expiration_date expirationDate,
            user_id userId,
            user_roles userRoles,
            usage_amount usageAmount,
            created FROM
            """+accessKeyTable+"\n";
    String selectAccessKeyCredential = """
            SELECT id,
            secret_key secretKey,
            status,"name",
            effective_date effectiveDate,
            expiration_date expirationDate,
            user_id userId,
            user_roles userRoles,
            usage_amount usageAmount,
            created FROM
            """+accessKeyTable+"\n";


//    @Select(selectAccessKeyCredential+"where user_id=#{userId} and id=#{accessKeyId}")
//    AIPortAccessKeyCredential selectAccessKeyCredentialById(@Param("userId")long userId,@Param("accessKeyId") int accessKeyId);

    @Select(selectAccessKeyCredential+"where id=#{accessKeyId}")
    AIPortAccessKeyCredential selectAccessKeyCredentialById(@Param("accessKeyId") long accessKeyId);


    @Select(selectAccessKey+"where secret_key=#{key}")
    AIPortAccessKey selectAccessKey(String key);
    @Select(selectAccessKey+" WHERE user_Id=#{userId} and id=#{id}")
    AIPortAccessKey selectAccessKeyById(@Param("userId")long userId,@Param("id") long id);

    @Select("<script>"+selectAccessKey+" WHERE user_Id=#{userId} AND id IN\n"+"""
                                <foreach item='item' index='index' collection='idList' open='(' separator=', ' close=')'>
                                #{item}
                                </foreach></script>""")
    List<AIPortAccessKey> selectAccessKeyByIds(@Param("userId")long userId,@Param("idList") List<Integer> idList);

    @Select(selectAccessKey+"where user_id=#{userId}")
    List<AIPortAccessKey> selectAccessKeyByUserId(long userId);

    @Insert("INSERT INTO "+accessKeyTable+
            "(id, secret_key,\"name\",status,effective_date, expiration_date, user_id, user_roles,created)VALUES" +
            "(#{id},#{secretKey},#{name},#{status},#{effectiveDate},#{expirationDate},#{userId},#{userRoles},#{created})")
    void insertAccessKeyCredential(AIPortAccessKeyCredential accessKey);

    @Update("UPDATE "+accessKeyTable+" SET name=#{name}, status=#{status} WHERE user_Id=#{userId} and id=#{id}")
    int updateAccessKey(AIPortAccessKey accessKey);

    @Update("UPDATE "+accessKeyTable+" SET name=#{name} WHERE user_Id=#{userId} and id=#{id}")
    int updateAccessKeyName(AIPortAccessKey accessKey);

    @Update("UPDATE "+accessKeyTable+" SET status=#{status} WHERE user_Id=#{userId} and id=#{id}")
    int updateAccessKeyStatus(AIPortAccessKey accessKey);

    @Delete("DELETE FROM "+accessKeyTable+" WHERE user_Id=#{userId} and id=#{id}")
    int deleteAccessKey(@Param("userId")long userId,@Param("id") long id);

    @Update("UPDATE "+accessKeyTable+" SET user_id=#{to} WHERE user_Id=#{from}")
    int transferAccessKeys(@Param("from")long from,@Param("to")long to);
}
