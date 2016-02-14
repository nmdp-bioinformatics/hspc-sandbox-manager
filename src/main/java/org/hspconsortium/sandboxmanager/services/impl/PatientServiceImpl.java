package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.Patient;
import org.hspconsortium.sandboxmanager.model.Persona;
import org.hspconsortium.sandboxmanager.repositories.PatientRepository;
import org.hspconsortium.sandboxmanager.services.PatientService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository repository;

    @Inject
    public PatientServiceImpl(final PatientRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Patient save(final Patient patient) {
        return repository.save(patient);
    }

    public Patient findByFhirId(String fhirId) {
        return  repository.findByFhirId(fhirId);
    }

}