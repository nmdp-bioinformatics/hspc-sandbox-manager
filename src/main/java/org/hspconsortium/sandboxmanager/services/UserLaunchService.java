package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.UserLaunch;

import java.util.List;

public interface UserLaunchService {

    UserLaunch save(final UserLaunch userLaunch);

    void delete(final int id);

    void delete(final UserLaunch userLaunch);

    UserLaunch create(final UserLaunch userLaunch);

    UserLaunch update(final UserLaunch userLaunch);

    UserLaunch getById(final int id);

    UserLaunch findByUserIdAndLaunchScenarioId(final String ldapId, final int launchScenarioId);

    List<UserLaunch> findByUserId(final String ldapId);

    List<UserLaunch> findByLaunchScenarioId(final int launchScenarioId);
}
