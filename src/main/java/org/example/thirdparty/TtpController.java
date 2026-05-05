package org.example.thirdparty;

import lombok.RequiredArgsConstructor;
import org.example.thirdparty.Crypto.SecurityService;
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

    @GetMapping(path = "/publickey", produces = "text/plain")
    public ResponseEntity<byte[]> getPublicKey(){
        return ok(securityService.getPublicKey().getEncoded());
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

    @PostMapping(path ="/auth", consumes = "application/json", produces = "text/plain")
    public ResponseEntity<String> auth(@RequestBody ClientAuthDto payload){
        try{
            return ResponseEntity.ok(securityService.clientAuthorization(payload));
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
        return ResponseEntity.ok(securityService.getCLients());
    }
}
