package org.bsk_project.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/logic")
public class ClientLogicController {

    private ClientService clientService;
    public ClientLogicController(ClientService clientService) {
        this.clientService = clientService;
    }

    /**
     * Listens for TTP Authentication Request with (sessionId)
     * @return
     */
    @PostMapping(path = "/auth", consumes = "text/plain", produces = "application/json")
    public ResponseEntity<String> getAuth(@RequestBody String sessionId) {
        try{
            clientService.authenticate(sessionId);
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }
//    @GetMapping("")
//    public ResponseEntity<String> getCreds(){
//
//    }


}
