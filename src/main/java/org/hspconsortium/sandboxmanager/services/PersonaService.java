package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.Persona;

public interface PersonaService {

    Persona save(final Persona persona);

    Persona findByFhirIdAndSandboxId(final String fhirId, final String sandboxId);
}
