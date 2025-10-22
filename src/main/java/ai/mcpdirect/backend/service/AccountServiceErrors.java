package ai.mcpdirect.backend.service;

public interface AccountServiceErrors {
    int ACCOUNT_NOT_EXIST = 1001000;
    int ACCOUNT_EXISTED = 1001001;

    int PASSWORD_INCORRECT = 1001002;

    int SIGN_IN_FAILED = 1001003;

    int TEAM_NOT_EXIST = 1002000;
}
