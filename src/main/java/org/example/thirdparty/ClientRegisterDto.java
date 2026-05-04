package org.example.thirdparty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Builder
public record ClientRegisterDto(String publicKey, UUID clientId){
    public ClientRegisterDto{
        Objects.requireNonNull(clientId, "clientId is null");
        Objects.requireNonNull(publicKey, "publicKey is null");
    }
}
