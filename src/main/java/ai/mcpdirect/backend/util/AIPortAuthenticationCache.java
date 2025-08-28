package ai.mcpdirect.backend.util;

import ai.mcpdirect.backend.dao.entity.account.AIPortAccessKeyCredential;
import ai.mcpdirect.backend.dao.entity.account.AIPortAccount;

import java.util.concurrent.ConcurrentHashMap;

public class AIPortAuthenticationCache {
    private final ConcurrentHashMap<Long, AIPortAccessKeyCredential> secretKeyCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, AIPortAccount> accountCache = new ConcurrentHashMap<>();
    public void add(AIPortAccount account){
        accountCache.computeIfAbsent(account.id, key->account);
    }
    public void add(AIPortAccessKeyCredential secretKey){
        long secretKeyHash = AIPortAccessKeyValidator.hashCode(secretKey.secretKey);
        secretKeyCache.computeIfAbsent(secretKeyHash,key->secretKey);
    }
    public AIPortAccessKeyCredential get(String secretKey){
        return secretKeyCache.get(AIPortAccessKeyValidator.hashCode(secretKey));
    }
    public AIPortAccount get(long userId){
        return accountCache.get(userId);
    }

}
