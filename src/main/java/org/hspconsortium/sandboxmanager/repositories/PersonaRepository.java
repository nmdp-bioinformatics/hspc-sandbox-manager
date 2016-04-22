package org.hspconsortium.sandboxmanager.repositories;

import org.hspconsortium.sandboxmanager.model.Persona;
import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PersonaRepository  extends CrudRepository<Persona, Integer> {
    public Persona findByFhirId(@Param("id") String id);
    public Persona findByFhirIdAndSandboxId(@Param("id") String id, @Param("sandboxId") String sandboxId);
}
