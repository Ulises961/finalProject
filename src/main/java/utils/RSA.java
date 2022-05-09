package utils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

public class RSA {

    public static void main(String[] args) {
        RSA rsa = new RSA();
        RSAKeys keys = rsa.generateKeys();
        BigInteger[] encryptedMessage = rsa.encrypt("security", keys.e, keys.n);
        System.out.println("encrypted message " + Arrays.toString(encryptedMessage));
        System.out.println(rsa.decrypt(encryptedMessage, keys.d, keys.n));
    }

    private static int generateRandomPrime() {
        int num = 0;
        Random rand = new Random(); // generate a random number
        num = rand.nextInt(1000) + 1;

        while (!isPrime(num)) {
            num = rand.nextInt(1000) + 1;
        }
        return num;
    }

    /**
     * Checks to see if the requested value is prime.
     */
    private static boolean isPrime(int inputNum) {
        if (inputNum <= 3 || inputNum % 2 == 0)
            return inputNum == 2 || inputNum == 3; // this returns false if number is <=1 & true if number = 2 or 3
        int divisor = 3;
        while ((divisor <= Math.sqrt(inputNum)) && (inputNum % divisor != 0))
            divisor += 2; // iterates through all possible divisors
        return inputNum % divisor != 0; // returns true/false
    }

    public RSAKeys generateKeys() {

        // generate two random prime numbers p and q.

    /*    int p = 17;// generateRandomPrime();
        int q = 23;// generateRandomPrime();
*/
        int p = generateRandomPrime();
        int q = generateRandomPrime();

        // calculate n = p*q
        int n = p * q;
        // calculate phi = (p-1)*(q-1)
        int phi = (p - 1) * (q - 1);
        // compute e: the minimum number that is coprime with phi greater than 1 and
        // lower than phi
        int e = 2;
        while (e < phi) {
            if (gcd(phi, e) == 1) {
                break;
            }
            e++;
        }

        // compute d with the Extended Euclidean algorithm

        // Extended Euclidean Algorithm Tip:

        int[] ps = {0, 1};
        int quotient = 0;
        int dividend = phi;
        int divisor = e;
        int remainder = phi;
        int pi = 0;
        int p0 = ps[0];
        int p1 = ps[1];
        int pN = 0;
        while (remainder > 0) {

            while (dividend < 0)
                dividend += divisor;
            quotient = dividend / divisor;

            remainder = dividend % divisor;
            int posP1 = p0 - p1 * quotient;

            while (posP1 < 0)
                posP1 += phi;

            pN = posP1 % phi;

            dividend = divisor;
            divisor = remainder;
            p0 = p1;
            p1 = pN;

        }
        int d = p0;
        System.out.println("Public key 'e' = " + e);
        System.out.println("Private key 'd' = " + d);
        System.out.println("'n= p*q' = " + n);
        BigInteger bigE = BigInteger.valueOf(e);
        BigInteger bigD = BigInteger.valueOf(d);
        BigInteger bigN = BigInteger.valueOf(n);

        return new RSAKeys(bigN, bigD, bigE);

    }

    private int gcd(int a, int b) {
        {
            // stores minimum(a, b)
            int i;
            if (a < b)
                i = a;
            else
                i = b;

            // take a loop iterating through smaller number to 1
            for (; i > 1; i--) {

                // check if the current value of i divides both
                // numbers with remainder 0 if yes, then i is
                // the GCD of a and b
                if (a % i == 0 && b % i == 0)
                    return i;
            }

            // if there are no common factors for a and b other
            // than 1, then GCD of a and b is 1
            return 1;
        }
    }

    public BigInteger[] encrypt(String plaintext, BigInteger e, BigInteger n) {
        BigInteger[] encrypted = new BigInteger[plaintext.length()];

        int asciiTableLength = 127;
        // plain text -> each character is converted into a number given by the position
        // of the character in the alphabet

        char[] letters = plaintext.toCharArray();
        int ascii;
        for (int j = 0; j < letters.length; j++) {
            BigInteger bigAscii = BigInteger.valueOf(letters[j]);

            int smallE = e.intValue();

            BigInteger bigPow = bigAscii.pow(smallE);
            BigInteger bigMod = bigPow.mod(n);
            encrypted[j] = bigMod;

            System.out.println("encrypted ascii " + encrypted[j]);


        }
        // for each number from the plaintext compute ( pow(number, e) ) mod n

        return encrypted;
    }

    public String decrypt(BigInteger[] ciphertext, BigInteger d, BigInteger n) {

        StringBuilder sb = new StringBuilder();

        for (int j = 0; j < ciphertext.length; j++) {

            BigInteger encryptedChar = ciphertext[j];
            BigInteger bigPow = encryptedChar.pow(d.intValue());
            BigInteger bigMod = bigPow.mod(n);
            System.out.printf("positionInAlphabet %d \n", bigMod.intValue());

            sb.append(Character.toString(bigMod.intValue()));

        }
        // for each number in the ciphertext compute ( pow(number, d) ) mod n

        // each resulting number is converted into a character assuming that this number
        // is the position of the character in the alphabet

        return sb.toString();
    }
}