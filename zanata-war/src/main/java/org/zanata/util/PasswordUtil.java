package org.zanata.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;

import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class PasswordUtil {
    /**
     * Used building output as Hex
     */
    private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
            'b', 'c', 'd', 'e', 'f' };

    /**
     * Deprecated in Seam. We need to do data migration if we want to use new
     * method.
     *
     * @deprecated use PasswordUtil.createPasswordKey() instead.
     */
    public static String generateSaltedHash(String password, String saltPhrase) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            if (saltPhrase != null) {
                md.update(saltPhrase.getBytes());
                byte[] salt = md.digest();

                md.reset();
                md.update(password.getBytes());
                md.update(salt);
            } else {
                md.update(password.getBytes());
            }

            byte[] raw = md.digest();
            return Base64.encodeBase64String(raw);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Copied from Seam.
     */
//    public static String createPasswordKey(@Nullable String hashAlgorithm,
//            char[] password, byte[] salt, int iterations)
//            throws GeneralSecurityException {
//        if (hashAlgorithm != null) {
//            PBEKeySpec passwordKeySpec =
//                    new PBEKeySpec(password, salt, iterations, 256);
//            SecretKeyFactory secretKeyFactory =
//                    SecretKeyFactory.getInstance(hashAlgorithm);
//            SecretKey passwordKey =
//                    secretKeyFactory.generateSecret(passwordKeySpec);
//            passwordKeySpec.clearPassword();
//            return BinTools.bin2hex(passwordKey.getEncoded());
//        } else {
//            PBKDF2Parameters params =
//                    new PBKDF2Parameters("HmacSHA1", "ISO-8859-1", salt,
//                            iterations);
//            PBKDF2 pbkdf2 = new PBKDF2Engine(params);
//            return BinTools.bin2hex(pbkdf2.deriveKey(new String(password)));
//        }
//    }

    /**
     * <b>copied from org.jboss.seam.util.Hex#encodeHex(byte[])</b>
     * Converts an array of bytes into an array of characters representing the
     * hexidecimal values of each byte in order. The returned array will be
     * double the length of the passed array, as it takes two characters to
     * represent any given byte.
     *
     * @param data
     *            a byte[] to convert to Hex characters
     * @return A char[] containing hexidecimal characters
     */
    public static char[] encodeHex(byte[] data) {

        int l = data.length;

        char[] out = new char[l << 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS[0x0F & data[i]];
        }

        return out;
    }
}
