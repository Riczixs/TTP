package org.bsk_project.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/logic")
public class ClientLogicController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ClientService clientService;
    public ClientLogicController(ClientService clientService) {
        this.clientService = clientService;
    }

    /**
     * Listens for TTP Authentication Request with (sessionId)
     * @return
     */
    @PostMapping(path = "/auth", consumes = "text/plain", produces = "application/json")
    public ResponseEntity<?> getAuth(@RequestBody String sessionId) {
        try{
            logger.info("Session init call from TTP {sessionId: {$sessionId}");
            clientService.authenticate(sessionId);
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            logger.warn("Error during authentication process");
            return ResponseEntity.notFound().build();
        }
    }
//    @GetMapping("")
//    public ResponseEntity<String> getCreds(){
//
//    }


}
