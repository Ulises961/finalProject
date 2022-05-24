package utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class CSRF {
    public static String getToken() throws NoSuchAlgorithmException {
        // generate random data
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        byte[] data = new byte[16];
        secureRandom.nextBytes(data);

        // convert to Base64 string
        return Base64.getEncoder().encodeToString(data);
    }
}
