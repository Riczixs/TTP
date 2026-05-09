package org.bsk_project.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api")
public class ServerController {
    private final Logger logger = LoggerFactory.getLogger(ServerController.class);
    private final ServService serverService;
    public ServerController(ServService serverService) {
        this.serverService = serverService;
    }

    /**
     * @STARTING_POINT_OF_CLIENT-SERVER_COMMUNICATION
     * @return
     */
    @GetMapping
    public ResponseEntity<?> getServer(){
        try{
            logger.info("Resource request received");
            serverService.initSession();
            logger.debug("Authentication successful");
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/register")
    public ResponseEntity<String> register(){
        try{
            var result = serverService.register();
            return ResponseEntity.ok(result);
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/auth")
    public ResponseEntity<String> auth(){
        try{
            var result = serverService.authenticate();
            logger.info("Server successfully authenticated");
            return ResponseEntity.ok(result);
        }catch (Exception e){
            logger.warn(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

}
