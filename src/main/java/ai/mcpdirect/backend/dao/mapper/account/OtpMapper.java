package ai.mcpdirect.backend.dao.mapper.account;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import ai.mcpdirect.backend.dao.entity.account.AIPortOtp;

public interface OtpMapper {
    String otpTable="account.otp";
    //pra_otp===================================
    @Select("select id, expiration_date expirationDate, account, otp from "+otpTable+" where id=#{id}")
    AIPortOtp selectOtp(@Param("id") long id);

    @Update("update "+otpTable+" set expiration_date=#{expirationDate} where id=#{id}")
    int updateOtpExpirationDate(AIPortOtp otp);

    @Update("update "+otpTable+" set otp=#{otp} where account=#{account}")
    int updateOtp(AIPortOtp otp);

    @Insert("INSERT INTO "+otpTable+" (id, expiration_date, account, otp)\n" +
            "VALUES(#{id}, #{expirationDate}, #{account}, #{otp});")
    void insertOtp(AIPortOtp otp);
    
}
