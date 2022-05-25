package utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Encryption {

    public static String encryptDigest(String encryptedBody, String sender, String privateKey, Connection conn) {
        String digest = Encryption.generateDigest(encryptedBody);

        BigInteger[] keys = Encryption.getKeys(sender, conn);
        RSA rsa = new RSA();

        BigInteger[] encryptedDigest = rsa.encrypt(digest, new BigInteger(privateKey), keys[1]);
        String newDigest = "" + encryptedDigest[0];

        for (int i = 1; i < encryptedDigest.length; i++)
            newDigest += "," + encryptedDigest[i];

        return newDigest;
    }

    public static String encryptMailBody(String body, String receiver, Connection conn) {
        BigInteger[] keys = getKeys(receiver, conn);
        RSA rsa = new RSA();

        BigInteger[] encryptedBody = rsa.encrypt(body, keys[0], keys[1]);
        String newBody = "" + encryptedBody[0];

        for (int i = 1; i < encryptedBody.length; i++)
            newBody += "," + encryptedBody[i];

        return newBody;
    }

    public static String decryptMessage(String message, BigInteger key, BigInteger n) {
        return (new RSA()).decrypt(message, key, n);
    }

    public static BigInteger[] getKeys(String receiver, Connection conn) {
        BigInteger[] keys = new BigInteger[2];

        try {
            String sendMail = "SELECT pub,n  FROM public_keys WHERE utente =?;";
            PreparedStatement pstm = conn.prepareStatement(sendMail);
            pstm.setString(1, receiver);

            ResultSet result = pstm.executeQuery();

            while (result.next()) {
                BigDecimal decimalPub = result.getBigDecimal("pub");
                BigInteger pub = decimalPub.toBigInteger();
                keys[0] = pub;

                BigDecimal decimalN = result.getBigDecimal("n");
                BigInteger n = decimalN.toBigInteger();
                keys[1] = n;

                System.out.println("I am returning the pub key");
                return keys;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("An error occurred while getting the pub key");

        return keys;
    }

    public static String generateDigest(String body) {
        try {
            // getInstance() method is called with algorithm SHA-512
            MessageDigest md = MessageDigest.getInstance("SHA-512");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(body.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
