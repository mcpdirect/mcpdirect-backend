package ai.mcpdirect.backend.dao.mapper.account;

import ai.mcpdirect.backend.dao.entity.account.*;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AccountMapper extends AccessKeyMapper,OtpMapper{
//    String otpTable = "account.otp";
    String userTable = "account.user";
    String userAccountTable = "account.user_account";

    String adminTable = "account.admin";
    String adminAccountTable = "account.admin_account";

    String anonymousTable = "account.user_anonymous";
    String anonymousTableFields = "id,secret_key,device_id";
    String anonymousTableSelectFields = "id,secret_key secretKey,device_id deviceId";

    //accounts.sdp_user_account===========================
    String selectUserAccountCredential="SELECT a.id,a.account, a.password, a.status FROM "
        +userAccountTable+" a\n";
    @Select(selectUserAccountCredential+"where a.id=#{id}")
    AIPortAccountCredential selectUserAccountCredentialById(long id);

    @Select(selectUserAccountCredential+"where account=#{account}")
    AIPortAccountCredential selectUserAccountCredential(String account);

    String selectUserAccount="SELECT a.id,a.account, a.status FROM "
            +userAccountTable+" a\n";
    @Select(selectUserAccount+"where account=#{account}")
    AIPortAccount selectUserAccount(String account);

    @Select(selectUserAccount+"where id=#{id}")
    AIPortAccount selectUserAccountById(long id);

    String selectUser="SELECT a.id,a.\"name\",a.\"language\", a.created, a.\"type\" FROM "
            +userTable+" a\n";
    @Select(selectUser+"where a.id=#{id}")
    AIPortUser selectUser(long id);
    
    @Update("update "+userAccountTable+" set password=#{password} where id=#{id}")
    void updateUserAccountPassword(@Param("id") long id, @Param("password") String newPassword);

    @Insert("INSERT INTO "+userAccountTable+" (id,account, password,status)\n" +
            "VALUES(#{id}, #{account}, #{password},#{status});")
    void insertUserAccountCredential(AIPortAccountCredential account);

    @Delete("DELETE from "+userAccountTable+" WHERE id=#{id}")
    void deleteUserAccount(long id);

    @Select("select count(id) from "+userAccountTable+" where account=#{account}")
    int checkUserAccount(@Param("account") String account);

    //accounts.sdp_user====================================
    @Insert("INSERT INTO "+userTable+"(id, \"name\",\"language\", \"type\", created)VALUES (" +
            "#{id}, #{name}, #{language},#{type}, #{created})")
    void insertUser(AIPortUser userInfo);

    //account.admin
    String selectAdminAccountCredential="SELECT a.id,a.account, a.password,a.status FROM "+adminAccountTable+" a\n";
    @Select(selectAdminAccountCredential+"where a.account=#{account}")
    AIPortAdminAccountCredential selectAdminAccountCredential(String account);

    @Insert("INSERT INTO "+anonymousTable+" ("+anonymousTableFields+")\n" +
            "VALUES(#{id}, #{secretKey},#{deviceId});")
    void insertUserAnonymousCredential(AIPortAnonymousCredential anonymous);

    @Delete("DELETE from "+anonymousTable+" WHERE id=#{id}")
    void deleteUserAnonymous(long id);

    @Select("SELECT "+anonymousTableSelectFields+" FROM "+anonymousTable+" where device_id=#{deviceId}")
    AIPortAnonymous selectUserAnonymousByDeviceId(String deviceId);

    @Select("SELECT "+anonymousTableSelectFields+" FROM "+anonymousTable+" where id=#{id}")
    AIPortAnonymousCredential selectUserAnonymousCredential(long id);
}
