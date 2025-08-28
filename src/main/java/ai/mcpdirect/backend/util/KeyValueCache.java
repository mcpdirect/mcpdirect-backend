package ai.mcpdirect.backend.util;

public interface KeyValueCache {
    void set(String key,String value);
    void set(String key,String value,long expiration);
    String get(String key);
    void remove(String key);
}
