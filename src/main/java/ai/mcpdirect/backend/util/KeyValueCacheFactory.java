package ai.mcpdirect.backend.util;

public class KeyValueCacheFactory {
    private static KeyValueCache instance;
    public static KeyValueCache getInstance() {
        return instance;
    }
    public static void setInstance(KeyValueCache instance) {
        KeyValueCacheFactory.instance = instance;
    }
}
