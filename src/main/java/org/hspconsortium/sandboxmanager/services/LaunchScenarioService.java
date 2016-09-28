package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.ContextParams;
import org.hspconsortium.sandboxmanager.model.LaunchScenario;

import java.util.List;

public interface LaunchScenarioService {

    LaunchScenario save(final LaunchScenario launchScenario);

    void delete(final int id);

    void delete(final LaunchScenario launchScenario);

    LaunchScenario create(final LaunchScenario launchScenario);

    LaunchScenario update(final LaunchScenario launchScenario);

    LaunchScenario updateContextParams(final LaunchScenario launchScenario, final List<ContextParams> contextParams);

    Iterable<LaunchScenario> findAll();

    LaunchScenario getById(final int id);

    List<LaunchScenario> findBySandboxId(final String sandboxId);

    List<LaunchScenario> findByAppIdAndSandboxId(final int appId, final String sandboxId);

    List<LaunchScenario> findByUserPersonaIdAndSandboxId(final int userPersonaId, final String sandboxId);
}
