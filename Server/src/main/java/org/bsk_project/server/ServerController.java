package org.bsk_project.server;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api")
public class ServerController {

    public ServService serverService;
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
            serverService.initSession();
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/register")
    public ResponseEntity<String> register(){
        try{
            var result = serverService.register();
            return ResponseEntity.ok(result);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/auth")
    public ResponseEntity<String> auth(){
        try{
            var result = serverService.authenticate();
            return ResponseEntity.ok(result);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
