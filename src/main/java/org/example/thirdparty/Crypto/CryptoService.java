package org.example.thirdparty.Crypto;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.oer.OEROptional;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

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
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

public class CryptoService {

    public byte[] decodeBase64(String data) {
        Base64.Decoder decoder = Base64.getDecoder();
        var decodedCert = decoder.decode(data);
        return decodedCert;
    }
    public String encryptBase64(byte[] data) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(data);
    }

    /**
     *
     * @param publicKey
     * @param data
     * @return base64 string
     */
    public Optional<String> rsaEncrypt(PublicKey publicKey, byte[] data){
        try {
            Cipher encryptCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey); //Someone's PK
            var encryptedData = encryptCipher.doFinal(data);
            Base64.Encoder encoder = Base64.getEncoder();
            return Optional.of(encoder.encodeToString(encryptedData));
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
            JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BCFIPS");
            return Optional.of(new JcaX509CertificateConverter().setProvider("BCFIPS")
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
            certificate.verify(issuerPublicKey, "BCFIPS");
            System.out.println("VALID CERT");
        }catch (Exception exception){
            System.out.println("NOT VALID CERT");
        }
    }

    public Optional<String> certToPEM(X509Certificate certificate){
        try{
            StringWriter stringWriter = new StringWriter();
            JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter);
            pemWriter.writeObject((X509Certificate)certificate);
            pemWriter.close();
            return Optional.of(stringWriter.toString());
        }catch(IOException e){
            return Optional.empty();
        }
    }

    public Optional<X509Certificate> PEMToCert(String certificate){
        try{
            PEMParser parser = new PEMParser(new StringReader(certificate));
            X509CertificateHolder certHolder = (X509CertificateHolder) parser.readObject();
            return Optional.of(new JcaX509CertificateConverter().getCertificate(certHolder));
        }catch (IOException | CertificateException e){
            return Optional.empty();
        }
    }

    public Optional<String> publicKeyToPem(PublicKey publicKey) {
        try{
            StringWriter sWrt = new StringWriter();
            JcaPEMWriter pemWriter = new JcaPEMWriter(sWrt);
            pemWriter.writeObject(publicKey);
            pemWriter.close();
            return Optional.of(sWrt.toString());
        }catch(IOException e){
            return Optional.empty();
        }
    }

    public Optional<PublicKey> pemToPublicKey(byte[] publicKey) {
        try{
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BCFIPS");
            return Optional.of(keyFactory.generatePublic(new X509EncodedKeySpec(publicKey)));
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] pkObjectToBytes(PublicKey publicKey){
        return publicKey.getEncoded();
    }

    public Optional<PublicKey> pkBytesToObject(byte[] keyBytes){
        try{
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BCFIPS");
            return Optional.of(keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes)));
        }catch(GeneralSecurityException e){
            return Optional.empty();
        }
    }

    public Optional<X509Certificate> certDecode(byte[] certBytes) throws GeneralSecurityException {
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
