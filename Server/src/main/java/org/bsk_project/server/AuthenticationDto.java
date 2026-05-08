package org.bsk_project.server;
import java.util.List;

/**
 *
 * @param certificate base64 String of bytes
 * @param sessionId base64 String of bytes
 * @param callbacks List of addresses for TTP
 */
public record AuthenticationDto(String certificate, String sessionId, Iterable<String> callbacks) {
}
