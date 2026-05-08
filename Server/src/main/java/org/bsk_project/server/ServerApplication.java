package org.bsk_project.server;

import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bsk_project.server.Crypto.CryptoService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.JettyClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tools.jackson.core.io.JsonStringEncoder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.security.*;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

@SpringBootApplication
public class ServerApplication {
    public static Scanner scanner = new Scanner(System.in);

    public static JdkClientHttpRequestFactory getFactory(){
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(30_000);
        return requestFactory;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        Security.addProvider(new BouncyCastleFipsProvider());
        CryptoServicesRegistrar.setSecureRandom(new SecureRandom("C:DEFRND[SHA256];ENABLE{ALL}".getBytes()));
        SpringApplication.run(ServerApplication.class, args);
        String ttpPublicKey = "";
        String cert = "";
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4));
        var keyPair = generator.generateKeyPair();
        var id = UUID.randomUUID().toString();
        CryptoService cryptoService = new CryptoService();

        RestClient restClient = RestClient.builder()
                .requestFactory(getFactory())
                .baseUrl("http://localhost:8081/api")
                .build();
        String in;
        while(!(in = scanner.nextLine()).equals("exit")){
            System.out.println(in);
            if(in.equals("g")) {
                ttpPublicKey = restClient.get()
                        .uri("/publickey")
                        .accept(MediaType.TEXT_PLAIN)
                        .retrieve()
                        .body(String.class);
                System.out.println(ttpPublicKey);
            }
            else if(in.equals("r")) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> payload = new HashMap();
                var tpk = cryptoService.pkBytesToObject(cryptoService.decodeBase64(ttpPublicKey))
                        .orElseThrow(()-> new RuntimeException("Could not decrypt the key"));
                 var rsaId = cryptoService.rsaEncrypt(tpk,
                        id.getBytes()
                ).orElseThrow(() -> new RuntimeException("Error while encrypting the id"));
                payload.put("publicKey", cryptoService.encryptBase64(keyPair.getPublic().getEncoded()));
                payload.put("clientId", rsaId);
                try{
                    cert = restClient.post()
                            .uri("/register")
                            .body(mapper.writeValueAsString(payload))
                            .contentType(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .body(String.class);
                        System.out.println("Cert " +cert);
                }catch(Exception e){
                    e.printStackTrace();
                    //System.out.println("Error while encrypting the key");
                }
            }
            else if(in.equals("a")) {
                Map<String, String> payload = new HashMap();
                payload.put("clientId", cryptoService.rsaEncrypt(
                                                cryptoService.pkBytesToObject(
                                                        cryptoService.decodeBase64(ttpPublicKey)
                                                ).orElseThrow(() -> new RuntimeException("Could not decrypt the key")),
                                                id.getBytes()
                                        ).orElseThrow(()-> new RuntimeException("Could not encrypt the id"))
                            );
                payload.put("cert", cert);
                payload.put("sessionId", "");
                ObjectMapper mapper = new ObjectMapper();
                try {
                    var sesionKey = restClient.post()
                            .uri("/auth")
                            .body(mapper.writeValueAsString(payload))
                            .contentType(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .body(String.class);
                    System.out.println("Session object : " + sesionKey);
                }catch (Exception e){
                    System.out.println("Blad");
                }
            }
        }
    }

}
