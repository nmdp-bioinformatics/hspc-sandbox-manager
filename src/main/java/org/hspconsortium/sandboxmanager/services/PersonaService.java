package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.Persona;

import java.util.List;

public interface PersonaService {

    Persona save(final Persona persona);

    void delete(final int id);

    void delete(final Persona persona);

    Persona findByFhirIdAndSandboxId(final String fhirId, final String sandboxId);

    List<Persona> findBySandboxId(final String sandboxId);
}
