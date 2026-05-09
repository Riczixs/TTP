package org.bsk_project.server;
import lombok.Getter;
import lombok.Setter;
import org.bsk_project.server.Crypto.CryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger logger = LoggerFactory.getLogger(ServService.class);
    private final CryptoService cryptoService;
    private final HttpService httpService;
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
        try{
            Credentials.ttpPublicKey = httpService.getKey();
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public String register() {
        try{
            logger.info("Registering to TTP");
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> payload = new HashMap();
            var tpk = cryptoService.pkBytesToObject(
                        cryptoService.decodeBase64(Credentials.ttpPublicKey)
            );
            var rsaId = cryptoService.rsaEncrypt(
                            tpk,
                            Credentials.id.getBytes()
            );
            payload.put("publicKey", cryptoService.encodeBase64(Credentials.keyPair.getPublic().getEncoded()));
            payload.put("clientId", rsaId);
            Credentials.certificate = httpService.register(mapper.writeValueAsString(payload));
            return Credentials.certificate;
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String authenticate() {
        logger.debug("Server authentication process starting");
        try{
            Map<String, String> payload = new HashMap();
            payload.put("clientId", cryptoService.rsaEncrypt(
                            cryptoService.pkBytesToObject(
                                    cryptoService.decodeBase64(Credentials.ttpPublicKey)
                            ),
                            Credentials.id.getBytes()
                    )
            );
            payload.put("cert", Credentials.certificate);
            payload.put("sessionId", "");
            ObjectMapper mapper = new ObjectMapper();
            var session = httpService.authenticate(mapper.writeValueAsString(payload));
            JsonNode jsonNode = mapper.readTree(session);
            Credentials.sessionId = jsonNode.get("sessionId").asText();
            Credentials.sessionKey = cryptoService.encodeBase64(
                cryptoService.rsaDecrypt(
                    cryptoService.decodeBase64(jsonNode.get("sessionKey").asText()
                    ),
                    Credentials.keyPair.getPrivate()
                )
            );
            logger.info("New session credentials received");
            return session;
        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

        /**
         * 1. Client calls REST API to fetch some data
         * 2. Server starts authentication process, sending POST Request to TTP:
         * @see AuthenticationDto
         * 3. TTP Checks Certificate and create Session object
         * 4. TTP Response to (2), with (sessionId, sessionKey)
         * 5. Server saves credentials and response to (1) with 204 No Content (TTP Authenticated)
         * 6. Meanwhile, TTP Sends authentication request to callback url, with sessionId in the request body.
         * 7. Client response with auth credentials and provided sessionId.
         * 8. After successfull authentication TTP Respond with (sessionId, sessionKey)
         * 9. Client authomatically makes HttpCall for Server resource with sessionKey
         */
    public void initSession() {
        try{
            if(Credentials.ttpPublicKey == null) {
                getTtpPublicKey();
                logger.debug("TTP Public Key fetched!");
            }
            if(Credentials.certificate == null){
                    register();
                    logger.info("Server successfully registered!");
             }
            authenticate();
        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
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
