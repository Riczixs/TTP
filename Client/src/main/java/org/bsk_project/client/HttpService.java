package org.bsk_project.client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Service
public class HttpService {
    private final Logger logger = LoggerFactory.getLogger(HttpService.class);
    private final RestClient restClient;
    public static JdkClientHttpRequestFactory getRequestFactory() {
        var requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(30_000);
        return requestFactory;
    }
    public HttpService() {
        restClient = RestClient.builder()
                .requestFactory(getRequestFactory())
                .baseUrl("http://localhost:8082/api") //TTP Address
                .build();
    }

    public Optional<String> getKey(){
        var result = restClient.get()
                .uri("/publickey")
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .body(String.class);
        return Optional.ofNullable(result);
    }

    public Optional<String> register(String payload){
        var result = restClient.post()
                .uri("/register")
                .body(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
        return Optional.ofNullable(result);
    }

    public Optional<String> authenticate(String payload){
        var sessionCreds = restClient.post()
                .uri("/auth")
                .body(payload)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
        return Optional.ofNullable(sessionCreds);
    }

    public void initServerSession(){
        try{
            logger.debug("Making call to server");
            var response = restClient.get()
                    .retrieve()
                    .toBodilessEntity();
            if(!response.getStatusCode().is2xxSuccessful()){
                throw new RuntimeException("Error during server http call execution");
            }
        }catch (Exception e){
            throw new RuntimeException("Error during init server http call" + e.getMessage());
        }
    }
}
