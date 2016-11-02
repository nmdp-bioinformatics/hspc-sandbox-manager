package org.hspconsortium.sandboxmanager.repositories;

import org.hspconsortium.sandboxmanager.model.ConfigType;
import org.hspconsortium.sandboxmanager.model.Config;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConfigRepository extends CrudRepository<Config, Integer> {
    public List<Config> findByConfigType(@Param("configType") ConfigType configType);
}
