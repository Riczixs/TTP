package org.example.thirdparty;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Service
public class HttpService {
    private RestClient restClient;
    public static JdkClientHttpRequestFactory getRequestFactory() {
        var requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(30_000);
        return requestFactory;
    }
    public HttpService() {
        restClient = RestClient.builder()
                .requestFactory(getRequestFactory())
                .baseUrl("http://localhost:8082/api")
                .build();
    }

}
