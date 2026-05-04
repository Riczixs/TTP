package org.example.thirdparty;


import lombok.Builder;

public class ClientMapper {
    public Client dtoToClient(ClientRegisterDto dto){
        return Client.builder()
                .publicKey(dto.publicKey())
                .clientId(dto.clientId())
                .build();
    }
}
