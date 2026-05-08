package org.bsk_project.client.Crypto;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

public class CryptoService {

    /**
     *
     * @param data String of base64 encoded bytes
     * @return raw bytes
     */
    public byte[] decodeBase64(String data) {
        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(data);
    }

    /**
     *
     * @param data (raw bytes)
     * @return String of base64 encoded bytes
     */
    public String encryptBase64(byte[] data) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(data);
    }

    /**
     *
     * @param publicKey
     * @param data (raw bytes)
     * @return base64 string
     */
    public Optional<String> rsaEncrypt(PublicKey publicKey, byte[] data){
        try {
            Cipher encryptCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey); //Someone's PK
            return Optional.of(encryptBase64(encryptCipher.doFinal(data)));
        }catch (Exception exception){
            return Optional.empty();
        }
    }

    public Optional<byte[]> rsaDecrypt(byte[] data, PrivateKey privateKey){
        try{
            Cipher decryptCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
            return Optional.of(decryptCipher.doFinal((data)));
        }catch (Exception exception){
            return Optional.empty();
        }
    }

    public Optional<X509Certificate> createCertificate(PublicKey clientPublicKey, PrivateKey issuerPrivateKey){ //Someone public key
        try{
            X509v1CertificateBuilder cert = new JcaX509v1CertificateBuilder(
                    new X500Name("CN= ISSUER CA"),
                    BigInteger.valueOf(System.currentTimeMillis()),
                    new Date(System.currentTimeMillis() - 1000L * 5),
                    new Date(System.currentTimeMillis() + ExValues.THIRTY_DAYS),
                    new X500Name("CN = Issuer CA"),
                    clientPublicKey);
            JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA256withRSA");
            return Optional.of(new JcaX509CertificateConverter()
                    .getCertificate(
                            cert.build(
                                    signerBuilder.build(
                                            issuerPrivateKey
                                    )
                            )
                    )
            );
        }catch (Exception exception){
            return Optional.empty();
        }
    }

    public void verifyCertificate(X509Certificate certificate, PublicKey issuerPublicKey){
        try {
            certificate.verify(issuerPublicKey);
            System.out.println("VALID CERT");
        }catch (Exception exception){
            System.out.println("NOT VALID CERT");
        }
    }

    public Optional<PublicKey> pkBytesToObject(byte[] keyBytes){
        try{
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BCFIPS");
            return Optional.of(keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes)));
        }catch(GeneralSecurityException e){
            return Optional.empty();
        }
    }

    public Optional<X509Certificate> certBytesToObject(byte[] certBytes) throws GeneralSecurityException {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BCFIPS");
        InputStream certStream = new ByteArrayInputStream(certBytes);
        return Optional.of((X509Certificate) certFactory.generateCertificate(certStream));
    }

    public Optional<SecretKey> createSessionKey() {
        try{
            SecureRandom sr = new SecureRandom();
            KeyGenerator kg = KeyGenerator.getInstance("AES", "BCFIPS");
            kg.init(sr);
            SecretKey key = kg.generateKey();
            return Optional.of(key);
        }catch(NoSuchAlgorithmException |  NoSuchProviderException e){
            return Optional.empty();
        }
    }
}
