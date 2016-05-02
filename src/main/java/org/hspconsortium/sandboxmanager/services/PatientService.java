package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.Patient;

public interface PatientService {

    Patient save(Patient patient);

    Patient findByFhirIdAndSandboxId(String fhirId, String sandboxId);
}
