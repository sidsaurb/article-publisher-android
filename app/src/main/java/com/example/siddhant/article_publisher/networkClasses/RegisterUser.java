package com.example.siddhant.article_publisher.networkClasses;

import java.security.MessageDigest;
import java.util.Formatter;

/**
 * Created by siddhant on 31/1/16.
 */
public class RegisterUser {

    public String name;
    public String email;
    public String password;

    public RegisterUser(String name, String email, String password) {
        this.name = name;
        this.email = email;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(password.getBytes());
            this.password = toHexString(digest);
        } catch (Exception ignored) {
            this.password = "";
        }
    }

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

}
