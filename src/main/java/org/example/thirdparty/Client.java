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
/**
 *
 * @param publicKey pure X509 bytes of PublicKey object
 * @param cert pure PKCS1 bytes of X509Certificate object
 * @param clientId bytes of UUID
 */
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Lob
    @Column(name="public_key", columnDefinition = "BLOB")
    private byte[] publicKey;

    @Lob
    @Column(name="cert", columnDefinition = "BLOB")
    private byte[] cert;

    @Lob
    @Column(name = "client_id", columnDefinition = "BLOB")
    private byte[] clientId;
}
