package org.example.thirdparty;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "sessions")
@NoArgsConstructor
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID sessionId;

    @Lob
    @Column(name="part1", nullable = true, columnDefinition = "BLOB")
    private byte[] part1;

    @Lob
    @Column(name="part2", nullable = true, columnDefinition = "BLOB")
    private byte[] part2;

    @Lob
    @Column(name="session_key", nullable = true, columnDefinition = "BLOB")
    private byte[] sessionKey;
}
