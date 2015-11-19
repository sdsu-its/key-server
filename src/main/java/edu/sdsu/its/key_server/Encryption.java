package edu.sdsu.its.key_server;

import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;
import org.apache.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Encrypt and Decrypt the Parameters that are being saved to the DB.
 */
public class Encryption {
    private static Cipher ecipher;
    private static Cipher dcipher;

    /**
     * Initialize the Encryption and Decryption Ciphers.
     * The Encryption key is supplied via the Environment Variable "Param_Key".
     */
    public Encryption() {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(System.getenv("Param_Key"));
            SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "DES");

            ecipher = Cipher.getInstance("DES");
            dcipher = Cipher.getInstance("DES");

            // initialize the ciphers with the given key

            ecipher.init(Cipher.ENCRYPT_MODE, key);

            dcipher.init(Cipher.DECRYPT_MODE, key);
        } catch (Exception e) {
            Logger.getLogger(getClass()).error("Problem initializing the Cipher", e);
        }
    }

    /**
     * Generate Encryption Key
     */
    public static void main(String[] args) throws NoSuchAlgorithmException {
        SecretKey secretKey = KeyGenerator.getInstance("DES").generateKey();
        System.out.println(String.format("Key (Exclude Quotes): \"%s\"", Base64.getEncoder().encodeToString(secretKey.getEncoded())));
    }

    /**
     * Encrypt the Supplied String
     *
     * @param str {@link String} String to Encrypt
     * @return {@link String} Encrypted String
     */
    public String encrypt(String str) {
        try {
            byte[] utf8 = str.getBytes("UTF8");
            byte[] enc = ecipher.doFinal(utf8);
            enc = BASE64EncoderStream.encode(enc);

            return new String(enc);

        } catch (Exception e) {
            Logger.getLogger(getClass()).error(String.format("Problem Encrypting %s", str), e);
            return "";
        }
    }

    /**
     * Decrypt String
     *
     * @param str {@link String} Encrypted String
     * @return {@link String} Decrypted String
     */
    public String decrypt(String str) {
        try {
            byte[] dec = BASE64DecoderStream.decode(str.getBytes());
            byte[] utf8 = dcipher.doFinal(dec);

            return new String(utf8, "UTF8");

        } catch (Exception e) {
            Logger.getLogger(getClass()).error(String.format("Problem Decrypting %s", str), e);
        }

        return "";
    }
}