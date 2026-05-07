package org.example.thirdparty;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 *
 * @param sessionId - Encrypted with TTP Private Key
 * @param sessionKey - Encrypted with TTP Private Key
 */
@Builder
public record ClientSessionDto(String sessionId, String sessionKey) {
}
