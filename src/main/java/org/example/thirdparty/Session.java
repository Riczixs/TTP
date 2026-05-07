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

    @Column(name="part1", nullable = true)
    private UUID part1;

    @Column(name="part2", nullable = true)
    private UUID part2;

    @Column(name="session_key", nullable = true)
    private String sessionKey;
}
