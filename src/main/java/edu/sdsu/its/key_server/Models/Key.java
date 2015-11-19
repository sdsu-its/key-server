package edu.sdsu.its.key_server.Models;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Models a Key
 */
public class Key {
    private String application_key;
    private String application_name;
    private String permissions;

    public Key(String application_key, String application_name, String permissions) {
        this.application_key = application_key;
        this.application_name = application_name;
        this.permissions = permissions;
    }

    public String getApplication_key() {
        if (application_key == null) {
            application_key = new BigInteger(130, new SecureRandom()).toString(32);
        }

        return application_key;
    }

    public String getApplication_name() {
        return application_name;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }
}
