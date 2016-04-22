package org.hspconsortium.sandboxmanager.repositories;

import org.hspconsortium.sandboxmanager.model.App;
import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface AppRepository extends CrudRepository<App, Integer> {
    public App findByLaunchUri(@Param("uri") String uri);
    public App findByClientId(@Param("client_id") String clientId);
    public App findByLaunchUriAndSandboxId(@Param("uri") String uri, @Param("sandboxId") String sandboxId);
    public App findByClientIdAndSandboxId(@Param("client_id") String clientId, @Param("sandboxId") String sandboxId);

}
