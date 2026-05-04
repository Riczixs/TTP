package org.example.thirdparty.Crypto;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.Optional;

public class CryptoService {
    public Optional<byte[]> rsaEncrypt(PublicKey publicKey, byte[] data){
        try {
            Cipher encryptCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey); //Someone's PK
            return Optional.of(encryptCipher.doFinal(data));
        }catch (Exception exception){
            return Optional.empty();
        }
    }

    public Optional<byte[]> rsaDecrypt(byte[] data, PrivateKey privateKey){
        try{
            Cipher decryptCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
            return Optional.of(decryptCipher.doFinal(data));
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
            //TODO Fix this method
            StringWriter stringWriter = new StringWriter();
            JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter);
            pemWriter.writeObject(certificate);
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

    public byte[] pkEncode(PublicKey publicKey){
        return publicKey.getEncoded();
    }

    public Optional<PublicKey> pkDecode(byte[] keyBytes) throws GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BCFIPS");
        return Optional.of(keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes)));
    }

    public String publicKeyToPEM(PublicKey publicKey) throws IOException {
        StringWriter sWrt = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(sWrt);
        pemWriter.writeObject(publicKey);
        pemWriter.close();
        return sWrt.toString();
    }

    public Optional<PublicKey> pemToPublicKey(byte[] publicKey){
        try{
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BCFIPS");
            return Optional.of(keyFactory.generatePublic(new X509EncodedKeySpec(publicKey)));
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
