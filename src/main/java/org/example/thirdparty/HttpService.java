package org.example.thirdparty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.core.support.RepositoryMethodInvocationListener;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class HttpService {
    private WebClient webClient;
    private Logger logger = LoggerFactory.getLogger(HttpService.class);
    public static JdkClientHttpRequestFactory getRequestFactory() {
        var requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(30_000);
        return requestFactory;
    }
    public HttpService() {
        webClient = WebClient.builder()
                .baseUrl("http://localhost:8083/api") //Client URL
                .build();
    }
    /**
     * TODO Async call to Client for its credentials
     */
//    @Async
    public void initClientAuth(String sessionId){
        try{
            Mono<ResponseEntity<Void>> response = webClient.get()
                    .uri("/api/logic/auth")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, r -> {
                        logger.warn("Client auth init call failed with status code: " + r.statusCode());
                        throw new RuntimeException();
                    })
                    .toBodilessEntity();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
