package ai.mcpdirect.backend.dao.entity.account;

import ai.mcpdirect.backend.util.ID;

public class AIPortAccountCredential extends AIPortAccount{
    public String password;


    public AIPortAccountCredential() {

    }

    public AIPortAccountCredential(String account, int status, String password) {
        this(ID.nextId(), account, status,password);
    }
    public AIPortAccountCredential(long id, String account, int status, String password) {
        super(id, account, status);
        this.password = password;
    }
}
