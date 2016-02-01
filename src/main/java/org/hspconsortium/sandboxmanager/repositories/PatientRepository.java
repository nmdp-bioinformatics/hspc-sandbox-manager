package org.hspconsortium.sandboxmanager.repositories;

import org.hspconsortium.sandboxmanager.model.Patient;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends CrudRepository<Patient, Integer> {
    public Patient findByFhirId(@Param("id") String id);
}
