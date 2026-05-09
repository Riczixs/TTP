package org.example.thirdparty;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;


@SpringBootApplication
@EnableAsync
public class ThirdPartyApplication {
    public static void main(String[] args) throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException, IOException {
        //BOUNCY CASTLE
        Security.addProvider(new BouncyCastleFipsProvider());
        CryptoServicesRegistrar.setSecureRandom(new SecureRandom("C:DEFRND[SHA256];ENABLE{ALL}".getBytes()));
        SpringApplication.run(ThirdPartyApplication.class, args);

    }
}
