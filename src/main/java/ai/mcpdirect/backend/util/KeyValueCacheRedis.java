package ai.mcpdirect.backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class KeyValueCacheRedis implements KeyValueCache{
    private static final Logger LOG = LoggerFactory.getLogger(KeyValueCacheRedis.class);
    private final JedisPool pool;
    public KeyValueCacheRedis(String host,int port,String password){
        // Create a JedisPoolConfig object
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        // --- Set pool size and other essential parameters ---
        // Maximum number of connections that can be allocated by the pool at any given time.
        // Default is 8. For most applications, this needs to be higher.
        poolConfig.setMaxTotal(100); 

        // Maximum number of idle connections that can be maintained in the pool.
        // Connections in excess of this amount will be closed and removed from the pool.
        // Default is 8. Often, you might set this similar to maxTotal, or slightly lower.
        poolConfig.setMaxIdle(50); 

        // Minimum number of idle connections that will always be maintained in the pool.
        // If the number of idle connections falls below this value, new connections will be created.
        // Default is 0. Setting a non-zero value helps avoid connection creation overhead during load spikes.
        poolConfig.setMinIdle(10); 

        // Maximum amount of time (in milliseconds) a client should wait for a connection to be available
        // when the pool is exhausted. If -1, it means wait indefinitely.
        poolConfig.setMaxWaitMillis(3000); // Wait up to 3 seconds

        // When true, the pool will block when `getResource()` is called and no connections are available.
        // If false, `getResource()` will throw an exception immediately.
        // Default is true.
        poolConfig.setBlockWhenExhausted(true); 

        // --- Optional: Connection validation settings (important for robust pools) ---
        // Test connections when they are borrowed from the pool.
        // Set to true to ensure you always get a live connection, but adds overhead.
        // Default is false.
        poolConfig.setTestOnBorrow(false); 

        // Test connections when they are returned to the pool.
        // Default is false.
        poolConfig.setTestOnReturn(false); 

        // Test connections while they are idle in the pool.
        // This is important for keeping connections alive and removing stale ones.
        // Requires timeBetweenEvictionRunsMillis to be set. Default is false.
        poolConfig.setTestWhileIdle(true); 

        // Time between runs of the idle object evictor thread.
        // Used in conjunction with setTestWhileIdle and minEvictableIdleTimeMillis.
        // Default is -1 (eviction thread not run).
        poolConfig.setTimeBetweenEvictionRunsMillis(60 * 1000); // Run evictor every 1 minute

        // Minimum amount of time a connection may remain idle in the pool before it is eligible for eviction.
        // Default is 30 minutes.
        poolConfig.setMinEvictableIdleTimeMillis(30 * 60 * 1000); // 30 minutes  

        pool = new JedisPool(poolConfig,host, port,2000,password);
        LOG.info("KeyValueCacheRedis.ping():{}",ping());
    }
    public String ping() {
        try (Jedis jedis = pool.getResource()) {
            return jedis.ping();
        } catch (Exception e) {
            return e.toString();
        }
    }
    @Override
    public void set(String key, String value) {
        try (Jedis jedis = pool.getResource()) {
            jedis.set(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void set(String key, String value, long expiration) {
        try (Jedis jedis = pool.getResource()) {
            jedis.setex(key, expiration, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String get(String key) {
        String value=null;
        try (Jedis jedis = pool.getResource()) {
            value = jedis.get(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    public void remove(String key) {
        String value=null;
        try (Jedis jedis = pool.getResource()) {
            jedis.del(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
