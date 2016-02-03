package org.hspconsortium.sandboxmanager.repositories;

import org.hspconsortium.sandboxmanager.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends CrudRepository<User, Integer> {
    public User findByLdapId(@Param("ldapId") String ldapId);
}
