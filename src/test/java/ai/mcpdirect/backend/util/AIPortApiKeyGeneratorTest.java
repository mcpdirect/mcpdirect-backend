package ai.mcpdirect.backend.util;
import org.junit.Test;

public class AIPortApiKeyGeneratorTest {

    @Test
    public void test() throws Exception {
        for(int i=0;i<10;i++) {
            String s = AIPortAccessKeyGenerator.generateApiKey("aik",1981661977485312L);
            System.out.println(s);
            System.out.println(AIPortAccessKeyValidator.hashCode(s));
//            System.out.println(AIPortAccessKeyGenerator.validateApiKey(s));
            System.out.println(AIPortAccessKeyValidator.extractUserId("aik",s));
        }
//        System.out.println(AIPortApiKeyGenerator.hashCode("aik-2MclQkNrkzfHDrN1qlizBoos5jpWfNQ603eejifuy0ecqo"));
//        System.out.println(AIPortApiKeyGenerator.hashCode("aik-KC0HboOnS8ffdMY5eNfgpUE0E6bPn0IE2637jifuy0ecqo"));
//        System.out.println(AIPortApiKeyGenerator.hashCode("aik-irXf1SBcIB21ERz070edWZbFiJa5ieczd5ebjifuy0ecqo"));
    }
    @Test
    public void testOrAnd(){
        long hash1 = AIPortAccessKeyValidator.hashCode("abcde");
        long hash2 = AIPortAccessKeyValidator.hashCode("12345");
        System.out.println(hash1+","+hash2);
        long or = hash1^hash2;
        System.out.println(or);
        System.out.println("hash1:"+(hash2^or)+",hash2:"+(hash1^or));
//        System.out.println("hash2"+(hash2&or));
    }
    @Test
    public void testHash(){
//        System.out.println(AIPortAccessKeyValidator.hashCode("aik-b6sb7R1wLDTLU5IOGI9HnGPzqr5lnmm263c7setacs2scmt8"));
        String[] keys = new String[]{
                "aik-uB1Kd9cPrqy2YCbTEN1nD0lW5xo9yGJ72e2914n5yxn0139zk",
                "aik-gUK805FY00nkucsf6MSFZ9oMcUm8FfqB0be2slhzsrk0hxt7",
                "aik-lNIgY6GNH4K2E03UIedVnWLz498mZYdnd7fcxfr6xiiogctj",
                "aik-b6sb7R1wLDTLU5IOGI9HnGPzqr5lnmm263c7setacs2scmt8"
        };
        for (String key : keys) {
            System.out.println(key.hashCode()&Integer.MAX_VALUE);
        }

    }

    @Test
    public void testPrimitive(){
        System.out.println(long.class.isPrimitive());
    }
}