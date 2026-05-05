package org.example.thirdparty;

import java.util.List;
import java.util.UUID;

public record ClientAuthDto(String cert, UUID clientId, List<String> callbacks) {
}
