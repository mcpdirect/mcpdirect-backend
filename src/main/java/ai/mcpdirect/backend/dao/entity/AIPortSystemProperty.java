package ai.mcpdirect.backend.dao.entity;

public class AIPortSystemProperty {
    public static final String OTP_EFFECTIVE_DURATION = "otpEffectiveDuration";
    // public static final String SIGN_UP_EMAIL_TEMPLATE = "signUpEmailTemplate";
    // public static final String SIGN_UP_SMS_TEMPLATE = "signUpSmsTemplate";
    // public static final String SIGN_UP_EMAIL_SUBJECT = "signUpEmailSubject";
    // public static final String SIGN_UP_SMS_SUBJECT = "signUpSmsSubject";
    public static final String FORGOT_PSD_EMAIL_TEMPLATE = "forgotPsdEmailTemplate";
    // public static final String FORGOT_PSD_SMS_TEMPLATE = "forgotPsdSmsTemplate";
    public static final String FORGOT_PSD_EMAIL_SUBJECT = "forgotPsdEmailSubject";
    // public static final String FORGOT_PSD_SMS_SUBJECT = "forgotPsdSmsSubject";

    public static final String OTP_EMAIL_TEMPLATE = "aiportOtpEmail";
    public static final String OTP_EMAIL_SUBJECT_TEMPLATE = "aiportOtpEmailSubject"; 

    public static final String STAGING_FORGOT_PSD_EMAIL_TEMPLATE = "stagingForgotPsdEmailTemplate";
    public static final String STAGING_FORGOT_PSD_EMAIL_SUBJECT = "stagingForgotPsdEmailSubject";

    public static final String AIPORT_SUPPORT_EMAIL = "aiportSupportEmail";
    public static final String AIPORT_DEFAULT_LANGUAGE = "aiportDefaultLanguage";
    public String key;
    public long archived;
    public long created;
    public int type;
    public String category="";
    public String value;
}
