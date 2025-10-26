package ai.mcpdirect.backend;

import appnet.hstp.ServiceEngine;
import appnet.hstp.ServiceEngineFactory;
import appnet.hstp.annotation.ServiceScan;
import ai.mcpdirect.backend.util.KeyValueCacheFactory;
import ai.mcpdirect.backend.util.KeyValueCacheRedis;

import appnet.hstp.exception.ServiceEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("ai.mcpdirect.backend")
@ServiceScan
public class MCPdirectBackendApplication implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger("MCPdirectBackendApplication");
    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(MCPdirectBackendApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
        ServiceEngineFactory.setServiceEngineIdSeed("ai.mcpdirect.backend");
        ServiceEngine serviceEngine = ServiceEngineFactory.getServiceEngine();
        LOG.info("ServiceEngine {} started",serviceEngine);
    }
    @Value("${spring.data.redis.host}")
    private String redisHost;
    @Value("${spring.data.redis.port}")
    private Integer redisPort;
    @Value("${spring.data.redis.password}")
    private String redisPassword;
    @Override
    public void run(String... args) throws Exception {
        String host=redisHost==null?"localhost":redisHost;
        int port = redisPort==null?6379:redisPort;
        if(redisPassword!=null&&(redisPassword=redisPassword.trim()).isEmpty()){
            redisPassword = null;
        }
        KeyValueCacheFactory.setInstance(new KeyValueCacheRedis(
            host,port,redisPassword
        ));

        LOG.info("MCPdirectBackendApplication started");
    }
}
