package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.AuthClient;

public interface AuthClientService {

    AuthClient save(final AuthClient authClient);

    void delete(final int id);

    AuthClient findByClientId(final String clientId);

}
