package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.UserRole;

public interface UserRoleService {

    void delete(final int id);

    void delete(final UserRole userRole);

    UserRole save(final UserRole userRole);

}
