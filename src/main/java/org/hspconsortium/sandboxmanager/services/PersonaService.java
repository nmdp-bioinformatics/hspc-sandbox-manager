package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.Persona;

public interface PersonaService {

    Persona save(Persona persona);

    Persona findByFhirIdAndSandboxId(String fhirId, String sandboxId);
}
