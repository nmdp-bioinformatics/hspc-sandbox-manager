package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.App;

import java.util.List;

public interface AppService {

    App save(final App app);

    void delete(final int id);

    App getById(final int id);

    App findByLaunchUriAndClientIdAndSandboxId(final String launchUri, final String clientId, final String sandboxId);

    List<App> findBySandboxId(final String sandboxId);

}
