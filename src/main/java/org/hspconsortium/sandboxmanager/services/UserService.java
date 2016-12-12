package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.User;

public interface UserService {

    User save(final User user);

    User findByLdapId(final String ldapId);

    void removeSandbox(final Sandbox sandbox, final User user);

    void addSandbox(final Sandbox sandbox, final User user);

    boolean hasSandbox(final Sandbox sandbox, final User user);

}
