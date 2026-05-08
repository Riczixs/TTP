package org.example.thirdparty.Crypto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.Bytes;
import org.bouncycastle.util.encoders.Base64Encoder;
import org.example.thirdparty.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import java.beans.Transient;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class SecurityService{
    private KeyPairGenerator generator;
    private KeyPair keyPair;
    private CryptoService cryptoService;
    public ClientMapper clientMapper;
    public TtpRepository ttpRepository;
    public SessionRepository sessionRepository;

    //For Spring ApplicationContext
    public String getPublicKey(){ //X.509
        return cryptoService.encryptBase64(keyPair.getPublic().getEncoded());
    }

    public SecurityService(TtpRepository ttpRepository, SessionRepository sessionRepository) {
        this.ttpRepository = ttpRepository;
        this.sessionRepository = sessionRepository;
        this.cryptoService = new CryptoService();
        this.clientMapper = new ClientMapper();
        try {
            generator = KeyPairGenerator.getInstance("RSA", "BCFIPS");
            generator.initialize(new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4));
            keyPair = generator.generateKeyPair();
        }catch(Exception exception){
            exception.printStackTrace();
        }
    }

    public PrivateKey getPrivateKey(){ //PKCS#8
        return keyPair.getPrivate();
    } //PKCS1

    /**
     *
     * @param clientDto (publicKey : base64 RSA-encoded String, clientId : base64 RSA-encoded String)
     * @return
     * @throws GeneralSecurityException
     */
    @Transactional
    public String clientRegister(ClientRegisterDto clientDto) throws GeneralSecurityException {
        //1 Checking if client of given id already exists
        var decodedId = cryptoService.decodeBase64(clientDto.clientId());
        var decryptedId = cryptoService.rsaDecrypt(decodedId, keyPair.getPrivate()).orElseThrow(() -> new RuntimeException());
        if(ttpRepository.existsByClientId(decryptedId)){
            throw new IllegalArgumentException();
        }
        //2 Register Client and generate Public Key Certificate
        var decodedKeyBytes = cryptoService.decodeBase64(clientDto.publicKey());
        X509Certificate cert = cryptoService.createCertificate(
                cryptoService.pkBytesToObject(decodedKeyBytes)
                        .orElseThrow(() -> new RuntimeException()),
                keyPair.getPrivate()
        ).orElseThrow(() -> new RuntimeException());
        Client c = Client.builder()
                .clientId(decryptedId)
                .publicKey(decodedKeyBytes)
                .cert(cert.getEncoded())
                .build();
        ttpRepository.save(c);
        return cryptoService.encryptBase64(cert.getEncoded());
    }

    /**
     *
     * @param clientDto (base64 String certificate, base64 String clientId, base64 String sessionId)
     * @return sessionKey
     * @throws GeneralSecurityException
     */
    @Transactional
    public ClientSessionDto clientAuthorization(ClientAuthDto clientDto) throws GeneralSecurityException {
        var cert = cryptoService.certBytesToObject(cryptoService.decodeBase64(clientDto.cert())).orElseThrow(() -> new RuntimeException());
        cryptoService.verifyCertificate(cert, keyPair.getPublic());
        //Create Session object
        var sk = cryptoService.createSessionKey().orElseThrow(() -> new RuntimeException());
        Session s = Session.builder()
                .part1(cryptoService.decodeBase64(clientDto.clientId()))
                .sessionKey(sk.getEncoded())
                .build();
        sessionRepository.save(s);

        var clientId = cryptoService.rsaDecrypt(
                            cryptoService.decodeBase64(clientDto.clientId()),
                            keyPair.getPrivate()
                        ).orElseThrow(() -> new RuntimeException());

        var client = ttpRepository.findByClientId(clientId).orElseThrow(() -> new RuntimeException());
        var clientPublicKey = cryptoService.pkBytesToObject(client.getPublicKey()).orElseThrow(() -> new RuntimeException());
        return ClientSessionDto.builder()
                .sessionId(cryptoService.rsaEncrypt(
                                clientPublicKey,
                                sk.getEncoded()
                            ).orElseThrow(() -> new RuntimeException())
                )
                .sessionKey(cryptoService.rsaEncrypt(
                                clientPublicKey,
                                sk.getEncoded()
                            ).orElseThrow(() -> new RuntimeException())
                ).build();
    }

    /**
     * Client
     * @param clientDto(base64 String Cert, base64 String ClientId, base64 String sessionId)
     * @return
     * @throws GeneralSecurityException
     */
    @Transactional
    public ClientSessionDto sessionAuthorization(ClientAuthDto clientDto) throws GeneralSecurityException {
        Base64.Decoder decoder = Base64.getDecoder();
        var decodedCert = decoder.decode(clientDto.cert());
        var cert = cryptoService.certBytesToObject(decodedCert).orElseThrow(() -> new RuntimeException());
        cryptoService.verifyCertificate(cert, keyPair.getPublic());
        var decodedSessionId = cryptoService.rsaDecrypt(null, keyPair.getPrivate()).orElseThrow(() -> new RuntimeException());
        Session s = sessionRepository.findById(UUID.nameUUIDFromBytes(decodedSessionId)).orElseThrow(() -> new RuntimeException());
        //Setting Session part2
        var decodedClientId = decoder.decode(clientDto.clientId());
        s.setPart2(decodedClientId);
        //Getting Existing SessionKey
        //DecodeRSA SessionKey
        var decoded = cryptoService.rsaDecrypt(null, keyPair.getPrivate());
        return ClientSessionDto.builder()
                .sessionId("")
                .build();
    }

    public Iterable<Client> getClients(){
        return ttpRepository.findAll();
    }
}
