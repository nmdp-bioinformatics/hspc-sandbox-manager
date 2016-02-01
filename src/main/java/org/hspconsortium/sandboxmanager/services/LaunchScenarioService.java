package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.LaunchScenario;

public interface LaunchScenarioService {

    LaunchScenario save(LaunchScenario launchScenario);

    void delete(LaunchScenario id);

    Iterable<LaunchScenario> findAll();
}
