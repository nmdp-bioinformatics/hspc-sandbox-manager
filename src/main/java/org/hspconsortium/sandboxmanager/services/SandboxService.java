package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.User;

public interface SandboxService {

    Sandbox save(final Sandbox sandbox);

    void delete(final int id);

    Sandbox findBySandboxId(final String sandboxId);
}
