package org.hspconsortium.sandboxmanager.repositories;

import org.hspconsortium.sandboxmanager.model.LaunchScenario;
import org.hspconsortium.sandboxmanager.model.Patient;
import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LaunchScenarioRepository extends CrudRepository<LaunchScenario, Integer> {
    public List<LaunchScenario> findByUserIdAndSandboxId(@Param("userId") String userId, @Param("sandboxId") String sandboxId);
}
