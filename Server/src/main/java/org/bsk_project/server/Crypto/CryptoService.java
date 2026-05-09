package org.bsk_project.server.Crypto;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

public class CryptoService {

    public byte[] decodeBase64(String data) {
        try{
            Base64.Decoder decoder = Base64.getDecoder();
            var decodedCert = decoder.decode(data);
            return decodedCert;
        }catch (Exception e) {
            throw new RuntimeException("Error during base64 DECODING -> " + e.getMessage());
        }
    }
    public String encodeBase64(byte[] data) {
        try{

            Base64.Encoder encoder = Base64.getEncoder();
            return encoder.encodeToString(data);
        }catch (Exception e) {
            throw new RuntimeException("Error during base64 ENCODING -> " + e.getMessage());
        }
    }
    /**
     *
     * @param publicKey
     * @param data
     * @return base64 string
     */
    public String rsaEncrypt(PublicKey publicKey, byte[] data){
        try {
            Cipher encryptCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey); //Someone's PK
            var encryptedData = encryptCipher.doFinal(data);
            return encodeBase64(encryptedData);
        }catch (Exception e){
            throw new RuntimeException("Error during rsa ENCRYPTION -> " + e.getMessage());
        }
    }

    public byte[] rsaDecrypt(byte[] data, PrivateKey privateKey){
        try{
            Cipher decryptCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
            return decryptCipher.doFinal((data));
        }catch (Exception e){
            throw new RuntimeException("Error during rsa DECRYPTION -> "+ e.getMessage());
        }
    }

    public byte[] pkObjectToBytes(PublicKey publicKey){
        return publicKey.getEncoded();
    }

    public PublicKey pkBytesToObject(byte[] keyBytes){
        try{
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
        }catch(GeneralSecurityException e){
            throw new RuntimeException("Error during ( byte[] -> PublicKey ) conversion -> "+ e.getMessage());
        }
    }

    public PrivateKey privkBytesToObject(byte[] keyBytes){
        try{
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        }catch (Exception e){
            throw new RuntimeException("Error during ( byte[] -> PrivateKey ) conversion -> " + e.getMessage());
        }
    }


}
