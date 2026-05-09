package org.bsk_project.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/front")
public class ClientFrontController {
    private final Logger logger = LoggerFactory.getLogger(ClientFrontController.class);
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
            logger.info("Client requested server resource");
            clientService.serverAuthentication();
            logger.debug("Server resource request sent");
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            logger.warn(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
