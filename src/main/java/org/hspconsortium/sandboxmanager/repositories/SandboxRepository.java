package org.hspconsortium.sandboxmanager.repositories;

import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.User;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;

public interface SandboxRepository extends CrudRepository<Sandbox, Integer> {
    public Sandbox findBySandboxId(@Param("sandboxId") String sandboxId);
}
