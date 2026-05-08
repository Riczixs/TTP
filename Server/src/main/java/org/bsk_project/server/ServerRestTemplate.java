package org.bsk_project.server;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ServerRestTemplate{

    private final WebClient webClient;

    public ServerRestTemplate() {
        this.webClient = WebClient.builder().baseUrl("http://localhost:8081/api").build();
    }
    public Mono<String> getTtpPublicKey(){
        var result = webClient.get()
                .uri("/publickey")
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .bodyToMono(String.class);
        return result;
    }

}
