package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.User;

public interface UserService {

    User save(final User user);

    User findByLdapId(final String ldapId);
}
