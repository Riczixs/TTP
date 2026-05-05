package org.example.thirdparty;


import lombok.Builder;

public class ClientMapper {
    public Client registerDtoToClient(ClientRegisterDto dto){
        return Client.builder()
                .publicKey(dto.publicKey())
                .clientId(dto.clientId())
                .build();
    }

    public Client authDtoToClient(ClientAuthDto dto){
        return Client.builder()
                .cert(dto.cert())
                .clientId(dto.clientId())
                .build();
    }

}
