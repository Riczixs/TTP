package org.bsk_project.server;
import lombok.Getter;
import lombok.Setter;
import org.bsk_project.server.Crypto.CryptoService;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ServService {
    private CryptoService cryptoService;
    private HttpService httpService;
    public ServService(HttpService httpService) {
        this.httpService = httpService;
        this.cryptoService = new CryptoService();
        try{
            var generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4));
            Credentials.keyPair = generator.generateKeyPair();
            Credentials.id = UUID.randomUUID().toString();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getTtpPublicKey(){
        Credentials.ttpPublicKey = httpService.getKey().orElseThrow(RuntimeException::new);
    }

    public String register() {
        try{
            getTtpPublicKey();
        }catch (Exception e) {
            throw new RuntimeException("Error getting TTP Public Key");
        }
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> payload = new HashMap();
        var tpk = cryptoService.pkBytesToObject(cryptoService.decodeBase64(Credentials.ttpPublicKey))
                .orElseThrow(()-> new RuntimeException("Could not decrypt the key"));
        var rsaId = cryptoService.rsaEncrypt(tpk,
                Credentials.id.getBytes()
        ).orElseThrow(() -> new RuntimeException("Error while encrypting the id"));
        payload.put("publicKey", cryptoService.encryptBase64(Credentials.keyPair.getPublic().getEncoded()));
        payload.put("clientId", rsaId);
        Credentials.certificate = httpService.register(mapper.writeValueAsString(payload)).orElseThrow(RuntimeException::new);
        return Credentials.certificate;
    }

    public String authenticate() {
        Map<String, String> payload = new HashMap();
        payload.put("clientId", cryptoService.rsaEncrypt(
                        cryptoService.pkBytesToObject(
                                cryptoService.decodeBase64(Credentials.ttpPublicKey)
                        ).orElseThrow(() -> new RuntimeException("Could not decrypt the key")),
                        Credentials.id.getBytes()
                ).orElseThrow(()-> new RuntimeException("Could not encrypt the id"))
        );
        payload.put("cert", Credentials.certificate);
        payload.put("sessionId", "");
        ObjectMapper mapper = new ObjectMapper();
        var session = httpService.authenticate(mapper.writeValueAsString(payload)).orElseThrow(RuntimeException::new);
        JsonNode jsonNode = mapper.readTree(session);
        Credentials.sessionId = jsonNode.get("sessionId").asText();
        Credentials.sessionKey = jsonNode.get("sessionKey").asText();
        return session;
    }


    @Getter
    @Setter
    public static class Credentials{
        private static KeyPair keyPair;
        private static String id;
        private static String ttpPublicKey;
        private static String certificate;
        private static String sessionId;
        private static String sessionKey;
    }
}
