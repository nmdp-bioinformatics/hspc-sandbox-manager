package org.hspconsortium.sandboxmanager.repositories;

import org.hspconsortium.sandboxmanager.model.Patient;
import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends CrudRepository<Patient, Integer> {
    public Patient findByFhirId(@Param("id") String id);
    public Patient findByFhirIdAndSandboxId(@Param("id") String id, @Param("sandboxId") String sandboxId);
}
