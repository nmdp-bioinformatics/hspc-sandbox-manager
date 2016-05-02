package org.hspconsortium.sandboxmanager.services;

import javax.servlet.http.HttpServletRequest;

/**
 */
public interface OAuthService {

    String getBearerToken(HttpServletRequest request);

    String getOAuthUserId(HttpServletRequest request);
}
