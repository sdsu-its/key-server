package edu.sdsu.its.key_server.Models;

import edu.sdsu.its.key_server.Encryption;

/**
 * Models a Parameter for an Application {@link App}
 */
public class Param {
    private String name;
    private String value;

    public Param(String name, String encryptedValue) {
        this.name = name;
        this.value = new Encryption().decrypt(encryptedValue);
    }

    public String getName() {
        return name;
    }

    public String getEncryptedValue() {
        return new Encryption().encrypt(value);
    }

    public String getDecryptedValue() {
        return value;
    }
}
