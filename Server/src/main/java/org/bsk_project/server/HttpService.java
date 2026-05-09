package org.bsk_project.server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Service
public class HttpService {
    private RestClient restClient;
    private final Logger logger = LoggerFactory.getLogger(HttpService.class);

    public static JdkClientHttpRequestFactory getRequestFactory() {
        var requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(30_000);
        return requestFactory;
    }
    public HttpService() {
        restClient = RestClient.builder()
                .requestFactory(getRequestFactory())
                .baseUrl("http://localhost:8081/api")
                .build();
    }

    public String getKey(){
        try{
            var result = restClient.get()
                    .uri("/publickey")
                    .accept(MediaType.TEXT_PLAIN)
                    .retrieve()
                    .body(String.class);
            return result;
        }catch(Exception e){
            throw new RuntimeException("Error retrieving TTP public key");
        }
    }

    public String register(String payload){
        try{
            var result = restClient.post()
                    .uri("/register")
                    .body(payload)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
            return result;
        }catch(Exception e){
            throw new RuntimeException("Error during registration to TTP");
        }
    }

    public String authenticate(String payload){
        try{
            var sessionCreds = restClient.post()
                    .uri("/auth")
                    .body(payload)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
            logger.info("Server's certificate successfully authenticated by TTP");
            return sessionCreds;
        }catch (Exception e){
            throw new RuntimeException("Error during authentication by TTP");
        }
    }
}
