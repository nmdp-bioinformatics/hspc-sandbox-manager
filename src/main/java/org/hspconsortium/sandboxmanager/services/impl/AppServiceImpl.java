package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.App;
import org.hspconsortium.sandboxmanager.repositories.AppRepository;
import org.hspconsortium.sandboxmanager.services.AppService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

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
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    public App getById(final int id) {
        return  repository.findOne(id);
    }

    @Override
    public App findByLaunchUriAndClientIdAndSandboxId(final String launchUri, final String clientId, final String sandboxId) {
        return repository.findByLaunchUriAndClientIdAndSandboxId(launchUri, clientId, sandboxId);
    }

    @Override
    public List<App> findBySandboxId(final String sandboxId){
        return  repository.findBySandboxId(sandboxId);
    }

}
