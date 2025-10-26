package ai.mcpdirect.backend.util;

import appnet.hstp.engine.util.JSON;
import org.junit.Test;

import static org.junit.Assert.*;

public class MailTest {

    public static void main(String[] args) throws Exception {
        Mail.Setting setting = JSON.fromJson("{\"name\":\"MCPdirect\",\"address\":\"noreply@mcpdirect.ai\",\"account\":\"mcpdirect.ai@gmail.com\",\"password\":\"\",\"server\":\"smtp.gmail.com\",\"port\":465,\"ssl\":true,\"tls\":false}",Mail.Setting.class);
        Mail.create(setting,"test","test","robin.shang@gmail.com").submit();

    }
}