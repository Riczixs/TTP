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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

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
import java.util.*;

@Service
public class SecurityService{
    private final Logger logger = LoggerFactory.getLogger(SecurityService.class);
    private KeyPair keyPair;
    private final CryptoService cryptoService;
    private final HttpService httpService;
    public ClientMapper clientMapper;
    public TtpRepository ttpRepository;
    public SessionRepository sessionRepository;

    public SecurityService(TtpRepository ttpRepository, SessionRepository sessionRepository) {
        this.ttpRepository = ttpRepository;
        this.sessionRepository = sessionRepository;
        this.cryptoService = new CryptoService();
        this.httpService = new HttpService();
        this.clientMapper = new ClientMapper();
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4));
            keyPair = generator.generateKeyPair();
        }catch(Exception exception){
            exception.printStackTrace();
        }
    }

    public PrivateKey getPrivateKey(){ //PKCS#8
        return keyPair.getPrivate();
    } //PKCS1

    //For Spring ApplicationContext
    public String getPublicKey(){ //X.509
        return cryptoService.encryptBase64(keyPair.getPublic().getEncoded());
    }

    /**
     *
     * @param clientDto (publicKey : base64 RSA-encoded String, clientId : base64 RSA-encoded String)
     * @return
     * @throws GeneralSecurityException
     */
    @Transactional
    public String clientRegister(ClientRegisterDto clientDto) throws GeneralSecurityException {
        //1 Checking if client of given id already exists
        try{
            var decodedId = cryptoService.decodeBase64(clientDto.clientId());
            var decryptedId = cryptoService.rsaDecrypt(decodedId, keyPair.getPrivate());
            if (ttpRepository.existsByClientId(decryptedId)) {
                throw new RuntimeException("Client with id " + decryptedId + " already exists");
            }
            //2 Register Client and generate Public Key Certificate
            var decodedKeyBytes = cryptoService.decodeBase64(clientDto.publicKey());
            X509Certificate cert = cryptoService.createCertificate(
                    cryptoService.pkBytesToObject(decodedKeyBytes),
                    keyPair.getPrivate()
            );
            logger.debug("New public key certificate created");
            Client c = Client.builder()
                    .clientId(decryptedId)
                    .publicKey(decodedKeyBytes)
                    .cert(cert.getEncoded())
                    .build();
            ttpRepository.save(c);
            logger.info("New client saved to database");
            return cryptoService.encryptBase64(cert.getEncoded());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     *
     * @param clientDto (base64 String certificate, base64 String clientId, base64 String sessionId)
     * @return sessionKey
     * @throws GeneralSecurityException
     */
    @Transactional
    public ClientSessionDto clientAuthorization(ClientAuthDto clientDto) throws GeneralSecurityException {
        try{
            var cert = cryptoService.certBytesToObject(cryptoService.decodeBase64(clientDto.cert()));
            cryptoService.verifyCertificate(cert, keyPair.getPublic());
            //Create Session object
            var sk = cryptoService.createSessionKey();
            Session s = Session.builder()
                    .part1(cryptoService.decodeBase64(clientDto.clientId()))
                    .sessionKey(sk.getEncoded())
                    .build();

            sessionRepository.save(s);

            var clientId = cryptoService.rsaDecrypt(
                    cryptoService.decodeBase64(clientDto.clientId()),
                    keyPair.getPrivate()
            );
            var client = ttpRepository.findByClientId(clientId).orElseThrow(() -> new RuntimeException("Client with id " + clientId + " not found"));
            var clientPublicKey = cryptoService.pkBytesToObject(client.getPublicKey());
        /**
         * @TODO Making http call to Client for authentication
         */
            ObjectMapper mapper = new ObjectMapper();
            httpService.initClientAuth(mapper.writeValueAsString(
                    Map.of("sessionId",s.getSessionId().toString())
                    )
            );
            logger.debug("Initialization call made successfully");
            return ClientSessionDto.builder()
                    .sessionId(s.getSessionId().toString())
                    .sessionKey(cryptoService.rsaEncrypt(
                                    clientPublicKey,
                                    sk.getEncoded()
                                )
                    )
                    .build();
        }catch (Exception e) {
            logger.error("Error during client auth initialization");
            throw new RuntimeException(e);
        }
    }

    /**
     * Client
     * @param clientDto(base64 String Cert, base64 String ClientId, base64 String sessionId)
     * @return
     * @throws GeneralSecurityException
     */
    @Transactional
    public ClientSessionDto sessionAuthorization(ClientAuthDto clientDto) throws GeneralSecurityException {
        var cert = cryptoService.certBytesToObject(cryptoService.decodeBase64(clientDto.cert()));
        cryptoService.verifyCertificate(cert, keyPair.getPublic());
        Session s = sessionRepository.findById(
                        UUID.nameUUIDFromBytes(
                            cryptoService.decodeBase64(clientDto.sessionId())
                        )
                    ).orElseThrow(() -> new RuntimeException("Session with id " + clientDto.sessionId() + " not found"));
        var decodedClientId = cryptoService.rsaDecrypt(
                cryptoService.decodeBase64(clientDto.clientId()),
                keyPair.getPrivate()
        );
        s.setPart2(decodedClientId);
        sessionRepository.save(s);
        var client = ttpRepository.findByClientId(decodedClientId).orElseThrow(() -> new RuntimeException("Client with id " + decodedClientId + " not found"));

        var encryptedSessionKey = cryptoService.rsaEncrypt(
                cryptoService.pkBytesToObject(
                    client.getPublicKey()
                ),
                s.getSessionKey()
        );
        return ClientSessionDto.builder()
                .sessionId(s.getSessionId().toString())
                .sessionKey(encryptedSessionKey)
                .build();
    }

    public Iterable<Client> getClients(){
        return ttpRepository.findAll();
    }
}
