package com.example.westinfo;


import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;

public class RsaBase64 {
    private final  static String appId = "10017";

    private final static String appSecret = "ABE07783AF757F86A81E04E95EC5CB09";

    private final static String publickey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAehk70IuupjGzjWb+lGDMZLm9PO0LAS/ns5cQqBIJb2RpGCM1mTwbcgv6s2mhI4Ap0Ugf9iOUnYYVs3SyfZW0jK9M7nCo6/WwsXpT9R8b3EmAsH9Nz30o2upKYbjKDqNigxZARlXfjS/ToAKN1KQ7mWCRsXqrGZepXw5EVmBWAQIDAQAB";

    public static void main(String[] args) {
        RSA  rsa = new RSA(null,publickey);

    String encrptSecret = rsa.encryptBase64(appSecret, CharsetUtil.CHARSET_UTF_8, KeyType.PublicKey);
    System.out.println(encrptSecret);

    }    }

