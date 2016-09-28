package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.Persona;
import org.hspconsortium.sandboxmanager.repositories.PersonaRepository;
import org.hspconsortium.sandboxmanager.services.PersonaService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class PersonaServiceImpl implements PersonaService {

    private final PersonaRepository repository;

    @Inject
    public PersonaServiceImpl(final PersonaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Persona save(final Persona persona) {
        return repository.save(persona);
    }
    @Override
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    @Transactional
    public void delete(final Persona persona) {
        delete(persona.getId());
    }

    @Override
    public Iterable<Persona> findAll(){
        return repository.findAll();
    }

    @Override
    public Persona findByFhirIdAndSandboxId(final String fhirId, final String sandboxId) {
        return  repository.findByFhirIdAndSandboxId(fhirId, sandboxId);
    }

    @Override
    public List<Persona> findBySandboxId(final String sandboxId) {
        return  repository.findBySandboxId(sandboxId);
    }
}


