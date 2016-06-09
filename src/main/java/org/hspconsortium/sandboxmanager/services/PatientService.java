package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.Patient;

public interface PatientService {

    Patient save(final Patient patient);

    Patient findByFhirIdAndSandboxId(final String fhirId, final String sandboxId);
}
