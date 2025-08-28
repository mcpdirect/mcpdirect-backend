package ai.mcpdirect.backend.util;

import java.security.SecureRandom;

public class AIPortAccessKeyGenerator {
    private static final String[] CHARACTERS = new String[]{
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
            "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
    };
    private static final SecureRandom secureRandom = new SecureRandom();
    // public static final String PREFIX_AIK = "aik";// AI/Tools access key
    // public static final String PREFIX_UTK = "auk";// User temp access key
    // private static final int KEY_PART_LENGTH = 32;
    // private static final int CHECKSUM_LENGTH = 4;
    public static String generateApiKey(String prefix,long userId) throws Exception {
        // 生成随机部分
        StringBuilder randomPartBuilder = new StringBuilder(AIPortAccessKeyValidator.KEY_PART_LENGTH);

        for (int i = 0; i < AIPortAccessKeyValidator.KEY_PART_LENGTH; i++) {
            int randomIndex = secureRandom.nextInt(CHARACTERS.length);
            randomPartBuilder.append(CHARACTERS[randomIndex]);
        }

        String randomPart = randomPartBuilder.toString();

        // 生成校验和
        String checksum = AIPortAccessKeyValidator.generateChecksum(randomPart);
        String keyPart = randomPart + checksum;
        long xor = AIPortAccessKeyValidator.hashCode(keyPart)^userId;
        return prefix+"-" + keyPart + Long.toString(xor,36);
    }
    public static String generateRandomKey() throws Exception {
        // 生成随机部分
        StringBuilder randomPartBuilder = new StringBuilder(AIPortAccessKeyValidator.KEY_PART_LENGTH);

        for (int i = 0; i < AIPortAccessKeyValidator.KEY_PART_LENGTH; i++) {
            int randomIndex = secureRandom.nextInt(CHARACTERS.length);
            randomPartBuilder.append(CHARACTERS[randomIndex]);
        }

        return randomPartBuilder.toString();
    }
    // public static Long extractUserId(String prefix,String apiKey){
    //     if(validateApiKey(prefix,apiKey)){
    //         String keyPart = apiKey.substring(prefix.length()+1);
    //         try {

    //             long xor = Long.parseLong(
    //                     keyPart.substring(KEY_PART_LENGTH + CHECKSUM_LENGTH),
    //                     36
    //             );
    //             return hashCode(keyPart.substring(0,KEY_PART_LENGTH + CHECKSUM_LENGTH))^xor;
    //         }catch (Exception ignore){}
    //     }
    //     return null;
    // }
    // private static String generateChecksum(String input) throws Exception {
    //     MessageDigest digest = MessageDigest.getInstance("SHA-256");
    //     byte[] hash = digest.digest(input.getBytes());
    //     return HexFormat.of().formatHex(hash).substring(0, CHECKSUM_LENGTH);
    // }

    // public static boolean validateApiKey(String prefix,String apiKey){
    //     if (apiKey == null || !apiKey.startsWith(prefix+"-")) {
    //         return false;
    //     }

    //     String keyBody = apiKey.substring(prefix.length()+1);
    //     if (keyBody.length() <= CHECKSUM_LENGTH) {
    //         return false;
    //     }

    //     String randomPart = keyBody.substring(0, KEY_PART_LENGTH);
    //     String checksum = keyBody.substring(KEY_PART_LENGTH,KEY_PART_LENGTH+CHECKSUM_LENGTH);
    //     try{
    //         return checksum.equals(generateChecksum(randomPart));
    //     }catch(Exception e){
    //         return false;
    //     }
    // }
    // public static long hashCode(String s) {
    //     if (s == null) {
    //         return 0L; // Or throw IllegalArgumentException
    //     }
    //     ;

    //     byte[] a = s.getBytes();
    //     int hashCode1 = Arrays.hashCode(a); // Standard 32-bit hash

    //     // A second hash code based on a shifted version of the string,
    //     // or a different prime multiplier, or simply reversing.
    //     // For simplicity, let's just use a modified version of the string.
    //     int hashCode2 = 1;
    //     int l = a.length-1;

    //     for (int i = l; i > 0; --i) {
    //         byte element = a[i];
    //         hashCode2 = 31 * hashCode2 + element;
    //     }
    //     // Combine the two 32-bit int hash codes into a 64-bit long
    //     return (((long) hashCode1 << 32) | (hashCode2 & 0xFFFFFFFFL))&Long.MAX_VALUE;
    //     // The `& 0xFFFFFFFFL` is important to ensure hashCode2 is treated as unsigned
    //     // when combined, preventing sign extension issues if hashCode2 is negative.
    // }
}