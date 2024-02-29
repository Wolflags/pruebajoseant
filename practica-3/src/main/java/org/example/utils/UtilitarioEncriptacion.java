package org.example.utils;
import  org.jasypt.util.text.AES256TextEncryptor;
public class UtilitarioEncriptacion {

    private static final String SECRET_KEY = "05162276";

    public static String encrypt(String data) {
        AES256TextEncryptor encryptor = new AES256TextEncryptor();
        encryptor.setPassword(SECRET_KEY);
        return encryptor.encrypt(data);
    }

    public static String decrypt(String encryptedData) {
        AES256TextEncryptor encryptor = new AES256TextEncryptor();
        encryptor.setPassword(SECRET_KEY);
        return encryptor.decrypt(encryptedData);
    }
}
