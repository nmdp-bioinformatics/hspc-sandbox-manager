package org.hspconsortium.sandboxmanager.services;

/**
 * Service to create and validate JWT
 */
public interface JwtService {

    String createSignedJwt(String subject);
}
