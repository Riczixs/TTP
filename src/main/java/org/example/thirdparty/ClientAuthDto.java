package org.example.thirdparty;

import java.util.List;
import java.util.UUID;

public record ClientAuthDto(String cert, String clientId, String sessionId) {
}
