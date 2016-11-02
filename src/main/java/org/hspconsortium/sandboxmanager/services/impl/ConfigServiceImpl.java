package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.ConfigType;
import org.hspconsortium.sandboxmanager.model.Config;
import org.hspconsortium.sandboxmanager.repositories.ConfigRepository;
import org.hspconsortium.sandboxmanager.services.ConfigService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class ConfigServiceImpl implements ConfigService {

    private final ConfigRepository repository;

    @Inject
    public ConfigServiceImpl(final ConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Config save(Config configuration) {
        return repository.save(configuration);
    }

    @Override
    public List<Config> findByConfigType(ConfigType configType) {
        return repository.findByConfigType(configType);
    }

}
