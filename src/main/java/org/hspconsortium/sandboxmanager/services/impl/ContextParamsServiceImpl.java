package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.ContextParams;
import org.hspconsortium.sandboxmanager.repositories.ContextParamsRepository;
import org.hspconsortium.sandboxmanager.services.ContextParamsService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
public class ContextParamsServiceImpl implements ContextParamsService {

    private final ContextParamsRepository repository;

    @Inject
    public ContextParamsServiceImpl(final ContextParamsRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ContextParams save(final ContextParams patient) {
        return repository.save(patient);
    }
}
