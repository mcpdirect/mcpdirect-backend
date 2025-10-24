package ai.mcpdirect.backend.service;

public interface AccountServiceErrors {
    int ACCOUNT_NOT_EXIST = 1001000;
    int ACCOUNT_EXISTED = 1001001;

    int PASSWORD_INCORRECT = 1001002;

    int SIGN_IN_FAILED = 1001003;
    int ACCOUNT_INCORRECT = 1001004;
    int OTP_EXPIRED = 1001005;
    int OTP_FAILED = 1001006;

    int TEAM_NOT_EXIST = 1002000;
}
