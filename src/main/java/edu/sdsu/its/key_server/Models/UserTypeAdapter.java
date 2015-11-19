package edu.sdsu.its.key_server.Models;

import com.google.gson.TypeAdapter;

import java.io.IOException;
import java.util.Base64;

public class UserTypeAdapter extends TypeAdapter<User> {
    @Override
    public User read(com.google.gson.stream.JsonReader in)
            throws IOException {
        final User user = new User("", "");

        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if ("username".equals(name)) {
                user.username = in.nextString();
            } else if ("email".equals(name)) {
                user.email = in.nextString();
            } else if ("password".equals(name)) {
                user.password = new String(Base64.getDecoder().decode(in.nextString().getBytes("UTF-8")));
            }
        }
        in.endObject();

        return user;
    }

    @Override
    public void write(com.google.gson.stream.JsonWriter out, User user)
            throws IOException {
        out.beginObject();
        out.name("username").value(user.username);

        if (!user.email.isEmpty()) {
            out.name("email").value(user.email);
        }

        out.endObject();
    }
}
