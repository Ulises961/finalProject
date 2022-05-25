package utils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CSRF {
    public static String getToken() throws NoSuchAlgorithmException {
        // generate random data
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        byte[] data = new byte[16];
        secureRandom.nextBytes(data);

        // convert to Base64 string
        return Base64.getEncoder().encodeToString(data);
    }

    public static boolean validateToken(HttpServletRequest request, HttpServletResponse response, String tokenToCheck) {

        String csrfCookie = null;
        Boolean isValidToken = false;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(tokenToCheck)) {
                    csrfCookie = cookie.getValue();
                }
            }
        }

        // get the CSRF form field
        String csrfField = request.getParameter(tokenToCheck);
        System.out.println("[" + tokenToCheck + "]: passed token: " + csrfField + " token stored in cookie " + csrfCookie);
        // validate CSRF
        if (csrfCookie == null || csrfField == null || !csrfCookie.equals(csrfField)) {
            System.out.println("csrfCookie: " + csrfCookie);
            System.out.println("csrfField: " + csrfField);
            System.out.println("!csrfCookie.equals(csrfField): " + (!csrfCookie.equals(csrfField)));
            try {
                response.sendError(401);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return isValidToken;
        }

        isValidToken = true;
        return isValidToken;
    }
}
