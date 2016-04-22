package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.User;

public interface SandboxService {

    Sandbox save(Sandbox sandbox);

    void delete(int id);

    Sandbox findBySandboxId(String sandboxId);
}
