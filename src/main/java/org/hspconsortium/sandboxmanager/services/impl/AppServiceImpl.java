package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.App;
import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.repositories.AppRepository;
import org.hspconsortium.sandboxmanager.services.AppService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
public class AppServiceImpl implements AppService {

    private final AppRepository repository;

    @Inject
    public AppServiceImpl(final AppRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public App save(final App app) {
        return repository.save(app);
    }

    @Override
    public App findByClientIdAndSandboxId(String clientId, String sandboxId) {
        return  repository.findByClientIdAndSandboxId(clientId, sandboxId);
    }

}
