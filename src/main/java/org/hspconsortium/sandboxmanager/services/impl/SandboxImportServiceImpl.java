package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.ContextParams;
import org.hspconsortium.sandboxmanager.model.SandboxImport;
import org.hspconsortium.sandboxmanager.repositories.ContextParamsRepository;
import org.hspconsortium.sandboxmanager.repositories.SandboxImportRepository;
import org.hspconsortium.sandboxmanager.services.ContextParamsService;
import org.hspconsortium.sandboxmanager.services.SandboxImportService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
public class SandboxImportServiceImpl implements SandboxImportService {

    private final SandboxImportRepository repository;

    @Inject
    public SandboxImportServiceImpl(final SandboxImportRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public SandboxImport save(final SandboxImport sandboxImport) {
        return repository.save(sandboxImport);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    @Transactional
    public void delete(final SandboxImport sandboxImport) {
        delete(sandboxImport.getId());
    }

}
