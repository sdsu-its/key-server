package edu.sdsu.its.key_server.Models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Models a API Admin User
 */
public class User {
    protected String username;
    protected String password;
    protected String email;

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
}

