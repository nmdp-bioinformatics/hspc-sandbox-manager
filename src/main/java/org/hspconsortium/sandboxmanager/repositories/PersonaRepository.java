package org.hspconsortium.sandboxmanager.repositories;

import org.hspconsortium.sandboxmanager.model.Persona;
import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PersonaRepository  extends CrudRepository<Persona, Integer> {
    public Persona findByFhirIdAndSandboxId(@Param("fhirId") String fhirId, @Param("sandboxId") String sandboxId);
}
