package ai.mcpdirect.backend.util;

import java.util.regex.Pattern;

public class ValidationUtils {
    public static final String FILE_NAME_REGEX = "^[a-zA-Z0-9!@#$%^&{}\\[\\]()_+\\-=,.~'` ]{1,255}$";
    public static final String DIRECTORY_NAME_REGEX = "^[a-zA-Z0-9:/\\\\!@#$%^&{}\\[\\]()_+\\-=,.~'` ]{1,255}$";
    public static boolean file(String fileName){
        return Pattern.matches(FILE_NAME_REGEX,fileName);
    }
    public static boolean directory(String dirName){
        return Pattern.matches(DIRECTORY_NAME_REGEX,dirName);
    }

    public static String EMAIL_REGEX = "^(?=.{1,64}@)[A-Za-z0-9+_-]+(\\.[A-Za-z0-9+_-]+)*@"
            + "[^-][A-Za-z0-9+-]+(\\.[A-Za-z0-9+-]+)*(\\.[A-Za-z]{2,})$";
    public static boolean email(String email){
        return Pattern.matches(EMAIL_REGEX,email);
    }
}
