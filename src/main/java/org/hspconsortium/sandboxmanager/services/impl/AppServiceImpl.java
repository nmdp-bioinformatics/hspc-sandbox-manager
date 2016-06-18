package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.App;
import org.hspconsortium.sandboxmanager.repositories.AppRepository;
import org.hspconsortium.sandboxmanager.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class AppServiceImpl implements AppService {

    private final AppRepository repository;
    private final AuthClientService authClientService;
    private final ImageService imageService;

    @Inject
    public AppServiceImpl(final AppRepository repository,
                          final AuthClientService authClientService,
                          final SandboxService sandboxService,
                          final ImageService imageService) {
        this.repository = repository;
        this.authClientService = authClientService;
        this.imageService = imageService;
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
    @Transactional
    public void delete(final App app) {

        if (app.getLogo() != null) {
            int logoId = app.getLogo().getId();
            app.setLogo(null);
            imageService.delete(logoId);
        }

        int authClientId = app.getAuthClient().getId();
        app.setAuthClient(null);
        save(app);
        authClientService.delete(authClientId);
        delete(app.getId());
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
