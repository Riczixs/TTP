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

    //Produces bytes of the new PublicKey Certificate
    @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Client> register(@RequestBody ClientRegisterDto payload){
        try{
            Client c = securityService.clientRegister(payload);
            return ResponseEntity.ok(c);
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(path = "/healthcheck", produces = "text/plain")
    public ResponseEntity<String> health(){
        return ok().build();
    }

}
