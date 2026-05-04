package org.example.thirdparty;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name="clients")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="public_key",unique = true)
    private String publicKey;

    private String cert;

    @Column(name = "client_id")
    private UUID clientId;
}
