package com.adfonic.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/*
 * This class contains a password hashing algorithm that has been adapted
 * from the example at http://www.owasp.org/index.php/Hashing_Java.
 */
public class PasswordUtils {
    private static final int NUMBER_OF_ITERATIONS = 1000;
    
    private PasswordUtils(){        
    }

    public static class PasswordAndSalt {
        private final String password;
        private final String salt;

        private PasswordAndSalt(String password, String salt) {
            this.password = password;
            this.salt = salt;
        }

        public String getPassword() {
            return password;
        }

        public String getSalt() {
            return salt;
        }
    }

    public static PasswordAndSalt encodePassword(String password) {
        try {
            // Uses a secure Random not a simple Random
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            // Salt generation 64 bits long
            byte[] bSalt = new byte[8];
            random.nextBytes(bSalt);
            // Digest computation
            byte[] bDigest = getHash(NUMBER_OF_ITERATIONS, password, bSalt);
            return new PasswordAndSalt(Base64.encode(bDigest), Base64.encode(bSalt));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean checkPassword(String entered, String password, String salt) {
        byte[] bDigest = Base64.decode(password);
        byte[] bSalt = Base64.decode(salt);

        // Compute the new DIGEST
        byte[] proposedDigest = PasswordUtils.getHash(NUMBER_OF_ITERATIONS, entered, bSalt);
        return Arrays.equals(proposedDigest, bDigest);
    }

    /**
     * From a password, a number of iterations and a salt, returns the
     * corresponding digest
     * 
     * @param iterationNb
     *            int The number of iterations of the algorithm
     * @param password
     *            String The password to encrypt
     * @param salt
     *            byte[] The salt
     * @return byte[] The digested password
     * @throws NoSuchAlgorithmException
     *             If the algorithm doesn't exist
     */
    private static byte[] getHash(int iterationNb, String password, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(salt);
            byte[] input = digest.digest(password.getBytes("UTF-8"));
            for (int i = 0; i < iterationNb; i++) {
                digest.reset();
                input = digest.digest(input);
            }
            return input;
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}
