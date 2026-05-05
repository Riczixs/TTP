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

    @Column(name="public_key",unique = true, length = 3000)
    private String publicKey;

    @Column(name="cert", length = 3000)
    private String cert;

    @Column(name = "client_id")
    private UUID clientId;
}
