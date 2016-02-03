package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.User;

public interface UserService {

    User save(User user);

    User findByLdapId(String ldapId);
}
