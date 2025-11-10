package com.example.westinfo;

import org.bouncycastle.asn1.gm.GMObjectIdentifiers;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

import java.security.*;
import java.util.Base64;

public class SM2Example {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) throws Exception {
        // 生成密钥对
        AsymmetricCipherKeyPair keyPair = generateKeyPair();
        ECPrivateKeyParameters privateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
        ECPublicKeyParameters publicKey = (ECPublicKeyParameters) keyPair.getPublic();

        // 将密钥转换为Base64编码字符串
        String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getQ().getEncoded(false));
        String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getD().toByteArray());

        System.out.println("publicKey::"+publicKeyBase64);
        System.out.println("privateKey::"+privateKeyBase64);
        // 测试加解密
        String plainText = "Hello, SM2!";
        System.out.println("明文: " + plainText);

        byte[] cipherText = encrypt(publicKey, plainText.getBytes());
        System.out.println("加密后: " + Base64.getEncoder().encodeToString(cipherText));

        byte[] decryptedText = decrypt(privateKey, cipherText);
        System.out.println("解密后: " + new String(decryptedText));
    }

    // 生成SM2密钥对
    public static AsymmetricCipherKeyPair generateKeyPair() throws NoSuchProviderException, NoSuchAlgorithmException {
        // 获取 SM2 曲线参数
        ECNamedCurveParameterSpec sm2Spec = ECNamedCurveTable.getParameterSpec("sm2p256v1");

        // 将参数封装为 ECNamedDomainParameters
        ECNamedDomainParameters domainParameters = new ECNamedDomainParameters(
                GMObjectIdentifiers.sm2p256v1,
                sm2Spec.getCurve(),
                sm2Spec.getG(),
                sm2Spec.getN());

        // 初始化密钥生成器
        ECKeyPairGenerator keyPairGenerator = new ECKeyPairGenerator();
        ECKeyGenerationParameters keyGenParams = new ECKeyGenerationParameters(domainParameters, new SecureRandom());
        keyPairGenerator.init(keyGenParams);

        // 生成密钥对
        return keyPairGenerator.generateKeyPair();
    }
    // 加密
    public static byte[] encrypt(ECPublicKeyParameters publicKey, byte[] data) throws Exception {
        SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C2C3);
        ParametersWithRandom params = new ParametersWithRandom(publicKey, new SecureRandom("EOL-ICT-TQM-2024-RSA".getBytes()));
        engine.init(true, params);
        return engine.processBlock(data, 0, data.length);
    }

    // 解密
    public static byte[] decrypt(ECPrivateKeyParameters privateKey, byte[] encryptedData) throws Exception {
        SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C2C3);
        engine.init(false, privateKey);
        return engine.processBlock(encryptedData, 0, encryptedData.length);
    }
}