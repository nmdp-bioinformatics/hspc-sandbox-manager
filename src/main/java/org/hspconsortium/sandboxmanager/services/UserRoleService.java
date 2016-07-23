package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.UserRole;

public interface UserRoleService {

    void delete(final int id);

    UserRole save(final UserRole userRole);

}
