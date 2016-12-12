package org.hspconsortium.sandboxmanager.repositories;

import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.User;
import org.hspconsortium.sandboxmanager.model.Visibility;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SandboxRepository extends CrudRepository<Sandbox, Integer> {
    public Sandbox findBySandboxId(@Param("sandboxId") String sandboxId);
    public List<Sandbox> findByVisibility(@Param("visibility") Visibility visibility);
}
