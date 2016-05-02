package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.LaunchScenario;
import org.hspconsortium.sandboxmanager.repositories.LaunchScenarioRepository;
import org.hspconsortium.sandboxmanager.services.LaunchScenarioService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class LaunchScenarioServiceImpl implements LaunchScenarioService {

    private final LaunchScenarioRepository repository;

    @Inject
    public LaunchScenarioServiceImpl(final LaunchScenarioRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public LaunchScenario save(final LaunchScenario launchScenario) {
        return repository.save(launchScenario);
    }

    @Override
    @Transactional
    public void delete(final LaunchScenario launchScenario) {
        repository.delete(launchScenario);
    }

    @Override
    public Iterable<LaunchScenario> findAll(){
        return repository.findAll();
    }

    @Override
    public LaunchScenario getById(int id) {
        return  repository.findOne(id);
    }

    @Override
    public List<LaunchScenario> findByUserIdAndSandboxId(String userId, String sandboxId) {
        return  repository.findByUserIdAndSandboxId(userId, sandboxId);
    }
}
