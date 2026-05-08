package org.bsk_project.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/front")
public class ClientFrontController {

    private ClientService clientService;
    public ClientFrontController(ClientService clientService) {
        this.clientService = clientService;
    }
    /**
     * Listens for Frontend calls
     * @return
     */
    @GetMapping
    public ResponseEntity<?> getResource() {
        try{
            clientService.serverAuthentication();
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }
}
