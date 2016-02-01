package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.Persona;
import org.hspconsortium.sandboxmanager.repositories.PersonaRepository;
import org.hspconsortium.sandboxmanager.services.PersonaService;
import org.springframework.stereotype.Service;


import javax.inject.Inject;
import javax.transaction.Transactional;

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
//        Persona existing = repository.findOne(persona.getId());
//        if (existing != null) {
////            throw new UserAlreadyExistsException(
////                    String.format("There already exists a user with id=%s", user.getId()));
//        } else {
        return repository.save(persona);
//        }
    }

    public Persona findByFhirId(String fhirId) {
        return  repository.findByFhirId(fhirId);
    }
}


