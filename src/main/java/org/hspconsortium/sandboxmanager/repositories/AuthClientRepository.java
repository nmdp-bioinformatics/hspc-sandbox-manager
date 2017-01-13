package org.hspconsortium.sandboxmanager.repositories;

import org.hspconsortium.sandboxmanager.model.AuthClient;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface AuthClientRepository extends CrudRepository<AuthClient, Integer> {
}
