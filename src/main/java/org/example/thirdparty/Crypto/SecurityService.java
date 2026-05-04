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
import org.bouncycastle.util.encoders.Base64Encoder;
import org.example.thirdparty.Client;
import org.example.thirdparty.ClientMapper;
import org.example.thirdparty.ClientRegisterDto;
import org.example.thirdparty.TtpRepository;
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

    //For Spring ApplicationContext
    public SecurityService(TtpRepository ttpRepository){
        this.ttpRepository = ttpRepository;
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

    public PublicKey getPublicKey(){ //X.509
        return keyPair.getPublic();
    } //X509

    public PrivateKey getPrivateKey(){ //PKCS#8
        return keyPair.getPrivate();
    } //PKCS1

    //PublicKey -> String PEM
    public Client clientRegister(ClientRegisterDto clientDto) throws GeneralSecurityException {
        Client c = clientMapper.dtoToClient(clientDto);
        Base64.Decoder decoder = Base64.getDecoder();
        var decodedKey = decoder.decode(clientDto.publicKey());
        X509Certificate cert = cryptoService.createCertificate(
                cryptoService.pkDecode(decodedKey)
                        .orElseThrow(() -> new RuntimeException()),
                keyPair.getPrivate()
        ).orElseThrow(() -> new RuntimeException());
        String certPEM = cryptoService.certToPEM(cert).orElseThrow(() -> new RuntimeException());
        c.setCert(certPEM);
        //Client clientResult = ttpRepository.save(c);
//        if(clientResult == null){
//            throw new RuntimeException();
//        }
        return c;
    }
}
