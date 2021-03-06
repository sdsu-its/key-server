package edu.sdsu.its.key_server.Models;

import com.google.gson.annotations.Expose;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Models a API Admin User
 */
public class User {
    @Expose
    private String username;
    @Expose
    private String email;
    @Expose(serialize = false)
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPasswordHash() {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] thedigest = md.digest(String.format(System.getenv("Hash_Salt"), password).getBytes());
            final StringBuilder sb = new StringBuilder();
            for (byte aThedigest : thedigest) {
                sb.append(Integer.toHexString((aThedigest & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public void clearPassword() {
        this.password = null;
    }

    public void updatePassword() {
        try {
            this.password = new String(Base64.getDecoder().decode(this.password.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger(getClass()).warn("Problem Decoding Password", e);
        }
    }
}

