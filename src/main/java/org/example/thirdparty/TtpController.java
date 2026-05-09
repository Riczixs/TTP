package org.example.thirdparty;

import lombok.RequiredArgsConstructor;
import org.example.thirdparty.Crypto.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;

import static org.springframework.http.ResponseEntity.ok;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class TtpController {
    private final SecurityService securityService;
    private final Logger logger = LoggerFactory.getLogger(TtpController.class);
    @GetMapping(path = "/publickey", produces = "text/plain")
    public ResponseEntity<String> getPublicKey(){
        return ResponseEntity.ok(securityService.getPublicKey());
    }
    /**
     *
     * @param payload (publicKey in UUID, clientId PEM string certificate)
     * @return PEM string certificate
     */
    @PostMapping(path = "/register", consumes = "application/json", produces = "text/plain")
    public ResponseEntity<String> register(@RequestBody ClientRegisterDto payload){
        try{
            String cert = securityService.clientRegister(payload);
            return ResponseEntity.ok(cert);
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     *
     * @param payload (String cert, UUID clientId, String sessionId)
     * @return
     */
    @PostMapping(path ="/auth", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ClientSessionDto> auth(@RequestBody ClientAuthDto payload){
        try{
            ClientSessionDto client = null;
            if(payload.sessionId().isEmpty()){ //First of session init
                client = securityService.clientAuthorization(payload);
                logger.debug("Client has been authenticated");
            }else{ //Second of session init
                client = securityService.sessionAuthorization(payload);
                logger.debug("New Session established!");
            }
            return ResponseEntity.ok(client);
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(path = "/healthcheck", produces = "text/plain")
    public ResponseEntity<String> health(){
        return ok().build();
    }

    @GetMapping(path = "/clients")
    public ResponseEntity<Iterable<Client>> getClients(){
        return ResponseEntity.ok(securityService.getClients());
    }
}
