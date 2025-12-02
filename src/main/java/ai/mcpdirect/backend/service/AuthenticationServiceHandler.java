package ai.mcpdirect.backend.service;

import ai.mcpdirect.backend.dao.entity.account.*;
import ai.mcpdirect.backend.util.*;
import appnet.hstp.annotation.*;
import ai.mcpdirect.backend.service.AccessKeyServiceHandler.RequestOfCreate;
import ai.mcpdirect.backend.dao.AccountDataHelper;
import ai.mcpdirect.backend.dao.mapper.account.AccountMapper;

import static ai.mcpdirect.backend.dao.entity.AIPortSystemProperty.*;
import static ai.mcpdirect.backend.service.AIPortServiceResponse.OTP_EXPIRED;
import static ai.mcpdirect.backend.service.AIPortServiceResponse.OTP_FAILED;

import appnet.hstp.ServiceEngine;
import appnet.hstp.SimpleServiceResponseMessage;
import appnet.util.crypto.SHA256;

@ServiceName("authentication")
@ServiceRequestMapping("/")
public class AuthenticationServiceHandler {
    private AccountMapper accountMapper;
    private AccountDataHelper helper;
    private ServiceEngine engine;

    @ServiceRequestInit
    public void init(ServiceEngine engine) {
        this.engine = engine;
        helper = AccountDataHelper.getInstance();
        accountMapper = helper.getAccountMapper();
    }

    public static class RequestOfRegister {
        public String account;
        public String otp;
        public long otpId;
        public AIPortUser userInfo;
    }

    @ServiceRequestMapping("register")
    public void register(
            @ServiceRequestMessage RequestOfRegister req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortOtp> resp) {
        try {
            if (req.account == null || (req.account = req.account.trim()).isEmpty()
                    || !ValidationUtils.email(req.account)) {
                resp.message = "invalid account";
            } else if (accountMapper.checkUserAccount(req.account)>0) {
                resp.message = "account existed";
                resp.code = AIPortServiceResponse.ACCOUNT_EXISTED;
            } else {
                int otpDuration = helper.getIntSystemProperty(OTP_EFFECTIVE_DURATION, 600000);
                resp.data = new AIPortOtp();
                if (req.otp != null) {
                    resp.code = OTP_EXPIRED;
                    resp.data.id = -1;
                    AIPortOtp aiOtp = accountMapper.selectOtp(req.otpId);
                    if (aiOtp != null && aiOtp.expirationDate > System.currentTimeMillis()
                            && aiOtp.otp.equals(req.otp)) {
                        resp.data.id = helper.executeSql((sqlSession -> {
                            AccountMapper mapper = sqlSession.getMapper(AccountMapper.class);

                            AIPortAccountCredential account = new AIPortAccountCredential();
                            account.account = req.account;
                            account.id = ID.nextId();
                            account.password = "";
                            account.status = 1;
                            account.keySeed = AIPortAccessKeyGenerator.generateRandomKey();

                            if (req.userInfo == null) {
                                req.userInfo = new AIPortUser();
                            }

                            req.userInfo.id = account.id;
                            if(req.userInfo.language==null||
                                    (req.userInfo.language=req.userInfo.language.trim()).isEmpty()){
                                req.userInfo.language = "en-US";
                            }
                            req.userInfo.created = System.currentTimeMillis();

                            mapper.insertUser(req.userInfo);

                            mapper.insertUserAccountCredential(account);

                            aiOtp.expirationDate = System.currentTimeMillis() + otpDuration;
                            mapper.updateOtpExpirationDate(aiOtp);

                            return req.otpId;
                        }));
                        resp.success();
                        // resp.data.expirationDate = aiOtp.expirationDate;
                    }
                } else {
                    resp.code = OTP_FAILED;
                    resp.data.id = -2;
                    AIPortOtp otp = AIPortOtp.createOtp(ID.nextId());
                    otp.account = req.account;
                    otp.expirationDate = System.currentTimeMillis() + otpDuration;
                    accountMapper.insertOtp(otp);

                    Mail.Setting supportEmail = helper.getObjectSystemProperty(AIPORT_SUPPORT_EMAIL,
                            Mail.Setting.class);

                    if (supportEmail != null) {
                        String defLang = helper.getSystemPropertyValue(AIPORT_DEFAULT_LANGUAGE,"en-US");
                        String lang = req.userInfo==null||req.userInfo.language==null?defLang:req.userInfo.language;
                        String content = helper.getStringSystemProperty(
                            OTP_EMAIL_TEMPLATE+"_"+lang);
                        if(content==null){
                            content = helper.getStringSystemProperty(
                                    OTP_EMAIL_TEMPLATE+"_"+defLang);
                        }
                        content = content.replace("${otp}", otp.otp)
                                .replace("${duration}",String.valueOf(otpDuration / 60000))
                                .replace("${detail}","");
                        String subject = helper.getStringSystemProperty(
                            OTP_EMAIL_SUBJECT_TEMPLATE+"_"+lang);
                        if(subject==null){
                            subject = helper.getStringSystemProperty(
                                    OTP_EMAIL_SUBJECT_TEMPLATE+"_"+defLang);
                        }

                        subject = subject.replace("${otp}", otp.otp);

                        Mail mail = Mail.create(supportEmail, subject, content, req.account);
                        mail.submit();

                        resp.data.id = otp.id;
                        resp.data.expirationDate = otp.expirationDate;
                        resp.success();
                    }
                }
            }
        } catch (Exception e) {
            resp.message = e.toString();
        }
    }

    public static class RequestOfForgotPassword extends RequestOfRegister {
        public String password;
    }

    @ServiceRequestMapping("forgot_password")
    public void forgotPassword(
            @ServiceRequestMessage RequestOfForgotPassword req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortOtp> resp) {
        try {
            AIPortAccount account;
            if (req.account == null || (req.account = req.account.trim()).isEmpty()
                    || !ValidationUtils.email(req.account)) {
                resp.message = "invalid account";
            } else if ((account=accountMapper.selectUserAccount(req.account))==null) {
                resp.message = "account not existed";
                resp.code = AIPortServiceResponse.ACCOUNT_NOT_EXIST;
            } else {
                int otpDuration = helper.getIntSystemProperty(OTP_EFFECTIVE_DURATION, 600000);
                resp.data = new AIPortOtp();
                if (req.otp != null) {
                    resp.code = OTP_EXPIRED;
                    resp.data.id = -1;
                    AIPortOtp aiOtp = accountMapper.selectOtp(req.otpId);
                    if (aiOtp != null && aiOtp.expirationDate > System.currentTimeMillis()
                            && aiOtp.otp.equals(req.otp)) {
                        accountMapper.updateUserAccountPassword(account.id, req.password);
                        resp.data.id = req.otpId;
                        resp.success();
//                        resp.data.id = helper.executeSql((sqlSession -> {
//                            AccountMapper mapper = sqlSession.getMapper(AccountMapper.class);
//                            mapper.updateUserAccountPassword(account.id, req.password);
//                            return req.otpId;
//                        }));
                        // resp.data.expirationDate = aiOtp.expirationDate;
                    }
                } else {
                    resp.code = OTP_FAILED;
                    resp.data.id = -2;
                    AIPortOtp otp = AIPortOtp.createOtp(ID.nextId());
                    otp.account = req.account;
                    otp.expirationDate = System.currentTimeMillis() + otpDuration;
                    accountMapper.insertOtp(otp);

                    Mail.Setting supportEmail = helper.getObjectSystemProperty(AIPORT_SUPPORT_EMAIL,
                            Mail.Setting.class);

                    if (supportEmail != null) {
                        String defLang = helper.getSystemPropertyValue(AIPORT_DEFAULT_LANGUAGE,"en-US");
                        String lang = req.userInfo==null||req.userInfo.language==null?defLang:req.userInfo.language;

                        String content = helper.getStringSystemProperty(
                            OTP_EMAIL_TEMPLATE+"_"+lang);
                        if(content==null){
                            content = helper.getStringSystemProperty(
                                    OTP_EMAIL_TEMPLATE+"_"+defLang);
                        }
                        content = content.replace("${otp}", otp.otp)
                                .replace("${duration}",String.valueOf(otpDuration / 60000))
                                .replace("${detail}","");
                        String subject = helper.getStringSystemProperty(
                            OTP_EMAIL_SUBJECT_TEMPLATE+"_"+lang);
                        if(subject==null){
                            subject = helper.getStringSystemProperty(
                                    OTP_EMAIL_SUBJECT_TEMPLATE+"_"+defLang);
                        }
                        subject = subject.replace("${otp}", otp.otp);

                        Mail mail = Mail.create(supportEmail, subject, content, req.account);
                        mail.submit();

                        resp.data.id = otp.id;
                        resp.data.expirationDate = otp.expirationDate;
                        resp.success();
                    }
                }
            }
        } catch (Exception e) {
            resp.message = e.toString();
        }
    }

    public static class RequestOfLogin {
        public String account;
        public String secretKey;
        public long timestamp;
//        public int userDevice;
    }

    public static class AccountDetails {
        public String account;
        public String accountKeySeed;
        public String accessToken;
        public int accessTokenType;
        public boolean newAccount;
        public AIPortUser userInfo;
    }

    public static boolean checkHash(String hash, String token, String key) {
        if (hash != null && token != null) {
            String check = token + key;
            return hash.equalsIgnoreCase(SHA256.digest(check))
                    || hash.equalsIgnoreCase(SHA256.digest(check.toLowerCase()))
                    || hash.equalsIgnoreCase(SHA256.digest(check.toUpperCase()));
        } else {
            return false;
        }
    }

    @ServiceRequestMapping("login")
    public void login(
            @ServiceRequestHeader("hstp-auth") String aiportDevice,
            @ServiceRequestMessage RequestOfLogin req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AccountDetails> resp) throws Exception {
        int userDevice = aiportDevice!=null?aiportDevice.hashCode():0;
        if (req.account == null) {
            resp.code = AIPortServiceResponse.SIGN_IN_FAILED;
            return;
        }
        resp.data = new AccountDetails();
        AIPortAccountCredential credentials = accountMapper.selectUserAccountCredential(req.account);
        if (credentials != null) {
            String password = credentials.password.toLowerCase();
            if (!checkHash(req.secretKey, password, Long.toString(req.timestamp))) {
                credentials = null;
            }
            if (credentials != null) {
                AccessKeyServiceHandler a = engine.getServiceRequestHandler(AccessKeyServiceHandler.class);

                SimpleServiceResponseMessage<String> ar = new SimpleServiceResponseMessage<>();
                a.create(new RequestOfCreate(credentials.id,userDevice), ar);

                if (ar.code == 0) {
                    // queryUserProperties(credentials.getId(), credentials.properties);
                    resp.data.account = credentials.account;
                    resp.data.accountKeySeed = credentials.keySeed;
                    resp.data.accessToken = ar.data;
                    resp.data.accessTokenType = AIPortAccount.PASSWORD;
                    resp.data.userInfo = accountMapper.selectUserById(credentials.id);
                    resp.success();
                }
            }
        }
        if (resp.data.accessToken == null) {
            resp.code = AIPortServiceResponse.SIGN_IN_FAILED;
        }
    }

    public static class RequestOfAnonymousLogin{
        public long id;
        public String secretKey;
        public long timestamp;
    }
    @ServiceRequestMapping("login/anonymous")
    public void login(
            @ServiceRequestHeader("hstp-auth") String aiportDevice,
            @ServiceRequestMessage RequestOfAnonymousLogin req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AccountDetails> resp) throws Exception {
        int userDevice = aiportDevice!=null?aiportDevice.hashCode():0;
        if (req.id == 0) {
            resp.code = AIPortServiceResponse.SIGN_IN_FAILED;
            resp.message = "invalid account info";
            return;
        }
        resp.data = new AccountDetails();
        AIPortAnonymousCredential credentials = accountMapper.selectUserAnonymousCredential(req.id);
        if (credentials != null) {
            String password = credentials.secretKey.toLowerCase();
            if (!checkHash(req.secretKey, password, Long.toString(req.timestamp))) {
                credentials = null;
            }
            if (credentials != null) {
                AccessKeyServiceHandler a = engine.getServiceRequestHandler(AccessKeyServiceHandler.class);

                SimpleServiceResponseMessage<String> ar = new SimpleServiceResponseMessage<>();
                a.create(new RequestOfCreate(credentials.id,userDevice), ar);

                if (ar.code == 0) {
                    // queryUserProperties(credentials.getId(), credentials.properties);
                    resp.data.account = "anonymous";
                    resp.data.accountKeySeed = credentials.keySeed;
                    resp.data.accessToken = ar.data;
                    resp.data.accessTokenType = AIPortAccount.ANONYMOUS;
                    resp.data.userInfo = accountMapper.selectUserById(credentials.id);
                    resp.success();
                }
            }
        }
        if (resp.data.accessToken == null) {
            resp.code = AIPortServiceResponse.SIGN_IN_FAILED;
        }
    }

    public static class RequestOfAnonymousRegister{
        public String deviceId;
        public AIPortUser userInfo;
    }

    @ServiceRequestMapping("register/anonymous")
    public void createAnonymousKey(
            @ServiceRequestMessage RequestOfAnonymousRegister req,
            @ServiceResponseMessage SimpleServiceResponseMessage<AIPortAnonymousCredential> resp
    ) throws Exception {
        if (req.deviceId != null&&!(req.deviceId= req.deviceId.trim()).isEmpty()
                &&(accountMapper.selectUserAnonymousByDeviceId(req.deviceId))==null) {
            long now = System.currentTimeMillis();
            String secretKey = AIPortAccessKeyGenerator.generateApiKey(
                    AIPortAccessKeyValidator.PREFIX_AAK, 0);

            AIPortAnonymousCredential credential = new AIPortAnonymousCredential(
                    AIPortAccessKeyValidator.hashCode(secretKey),
                    req.deviceId,SHA256.digest(secretKey),  1,
                    AIPortAccessKeyGenerator.generateRandomKey(16));

            if (req.userInfo == null) {
                req.userInfo = new AIPortUser();
            }
            req.userInfo.id = credential.id;
            if(req.userInfo.language==null||
                    (req.userInfo.language=req.userInfo.language.trim()).isEmpty()){
                req.userInfo.language = "en-US";
            }
            req.userInfo.name ="anonymous";
            req.userInfo.created = System.currentTimeMillis();
            req.userInfo.type = AIPortAccount.ANONYMOUS;

            helper.executeSql(sqlSession -> {
                AccountMapper mapper = sqlSession.getMapper(AccountMapper.class);
                mapper.insertUserAnonymousCredential(credential);
                mapper.insertUserAccountCredential(new AIPortAccountCredential(
                        credential.id,"anonymous@"+ credential.id, 1,
                        "",AIPortAccessKeyGenerator.generateRandomKey(16)));
                mapper.insertUser(req.userInfo);
                return true;
            });
            credential.secretKey = secretKey;
            resp.success(credential);
        }
    }
}
