package org.hspconsortium.sandboxmanager.repositories;

import org.hspconsortium.sandboxmanager.model.App;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface AppRepository extends CrudRepository<App, Integer> {
    public App findByLaunchUri(@Param("uri") String uri);
    public App findByClientId(@Param("client_id") String clientId);
}
