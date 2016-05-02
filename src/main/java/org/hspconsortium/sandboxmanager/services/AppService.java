package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.App;

public interface AppService {

    App save(App app);

    App findByClientIdAndSandboxId(String clientId, String sandboxId);

}
