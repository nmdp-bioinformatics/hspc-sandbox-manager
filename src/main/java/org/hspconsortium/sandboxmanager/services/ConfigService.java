package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.ConfigType;
import org.hspconsortium.sandboxmanager.model.Config;

import java.util.List;

public interface ConfigService {

    Config save(final Config configuration);

    List<Config> findByConfigType(final ConfigType configType);

}

