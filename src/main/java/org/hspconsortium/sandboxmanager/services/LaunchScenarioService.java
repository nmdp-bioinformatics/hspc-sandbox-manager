package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.LaunchScenario;

import java.util.List;

public interface LaunchScenarioService {

    LaunchScenario save(final LaunchScenario launchScenario);

    void delete(final int id);

    Iterable<LaunchScenario> findAll();

    LaunchScenario getById(final int id);

    List<LaunchScenario> findByUserIdAndSandboxId(final String userId, final String sandboxId);

}
