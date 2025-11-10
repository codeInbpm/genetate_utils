package com.example.westinfo;
import cn.hutool.json.JSONObject;
import org.python.antlr.ast.Str;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

public class AESWithSHA256 {
    public static void main(String[] args) throws Exception {
        String plainText = "Hello, AES with SHA-256!";
        System.out.println("原始数据: " + plainText);

        // 使用密码生成 Base64 编码的密钥字符串
        String password = "EOL-ICT-TQM-2024";
        String secretKeyStr = generateBase64KeyFromPassword(password);
        System.out.println("生成的密钥字符串: " + secretKeyStr);

        // 从字符串恢复密钥
        SecretKey secretKey = convertBase64KeyToSecretKey(secretKeyStr);

        // 加密
        String encryptedText = encrypt(secretKey, plainText);
        System.out.println("加密数据: " + encryptedText);

        // 解密
        String decryptedText = decrypt(secretKey, encryptedText);
        System.out.println("解密数据: " + decryptedText);
    }

    // 通过密码生成 Base64 编码的密钥字符串
    public static String generateBase64KeyFromPassword(String password) throws Exception {
        // 使用 SHA-256 生成密钥的字节数组
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(password.getBytes("UTF-8"));

        // 转换为 Base64 字符串并返回
        return Base64.getEncoder().encodeToString(keyBytes).substring(0, 24); // 截取 16 字节（128 位）密钥
    }

    // 从 Base64 编码的密钥字符串恢复 SecretKey
    public static SecretKey convertBase64KeyToSecretKey(String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(keyBytes, 0, 16, "AES");
    }

    // AES 加密
    public static String encrypt(SecretKey key, String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // AES 解密
    public static String decrypt(SecretKey key, String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes, "UTF-8");
    }
    class test{
        String a = "";

    }

}