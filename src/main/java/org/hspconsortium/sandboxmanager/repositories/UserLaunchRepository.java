package org.hspconsortium.sandboxmanager.repositories;

import org.hspconsortium.sandboxmanager.model.UserLaunch;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserLaunchRepository extends CrudRepository<UserLaunch, Integer> {
    public UserLaunch findByUserIdAndLaunchScenarioId(@Param("ldapId") String ldapId,
                                                      @Param("launchScenarioId") int launchScenarioId);

    public List<UserLaunch> findByUserId(@Param("ldapId") String ldapId);

    public List<UserLaunch> findByLaunchScenarioId(@Param("launchScenarioId") int launchScenarioId);
}
