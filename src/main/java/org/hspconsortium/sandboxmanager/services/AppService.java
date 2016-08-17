package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.App;
import org.hspconsortium.sandboxmanager.model.Image;

import java.util.List;

public interface AppService {

    App save(final App app);

    void delete(final int id);

    void delete(final App app);

    App create(final App app);

    App update(final App app);

    App getClientJSON(final App app);

    App updateAppImage(final App app, final Image image);

    App getById(final int id);

    App findByLaunchUriAndClientIdAndSandboxId(final String launchUri, final String clientId, final String sandboxId);

    List<App> findBySandboxId(final String sandboxId);

}
