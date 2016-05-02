package org.hspconsortium.sandboxmanager.services;

import javax.servlet.http.HttpServletRequest;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 */
public interface OAuthService {

    String getBearerToken(HttpServletRequest request);

    String getOAuthUserId(HttpServletRequest request);
}
