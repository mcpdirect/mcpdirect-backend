package ai.mcpdirect.backend.dao.entity.account;

import ai.mcpdirect.backend.util.ID;

public class AIPortAccountCredential extends AIPortAccount{
    public String password;


    public AIPortAccountCredential() {

    }

    public AIPortAccountCredential(String account, int status, String password,String keySeed) {
        this(ID.nextId(), account, status,password,keySeed);
    }
    public AIPortAccountCredential(long id, String account, int status, String password,String keySeed) {
        super(id, account, status,keySeed);
        this.password = password;
    }
}
