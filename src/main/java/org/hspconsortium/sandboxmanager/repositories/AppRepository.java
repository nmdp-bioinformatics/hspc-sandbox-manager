package org.hspconsortium.sandboxmanager.repositories;

import org.hspconsortium.sandboxmanager.model.App;
import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface AppRepository extends CrudRepository<App, Integer> {
    public App findByClientIdAndSandboxId(@Param("client_id") String clientId, @Param("sandboxId") String sandboxId);

}
