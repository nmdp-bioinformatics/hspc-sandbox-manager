package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.UserLaunch;
import org.hspconsortium.sandboxmanager.repositories.UserLaunchRepository;
import org.hspconsortium.sandboxmanager.services.UserLaunchService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class UserLaunchServiceImpl implements UserLaunchService {

    private final UserLaunchRepository repository;

    @Inject
    public UserLaunchServiceImpl(final UserLaunchRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public UserLaunch save(final UserLaunch userLaunch) {
        return repository.save(userLaunch);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }

    @Override
    @Transactional
    public void delete(final UserLaunch userLaunch) {
        delete(userLaunch.getId());
    }

    @Override
    @Transactional
    public UserLaunch create(final UserLaunch userLaunch) {
        return save(userLaunch);
    }

    @Override
    public UserLaunch getById(final int id) {
        return  repository.findOne(id);
    }

    @Override
    @Transactional
    public UserLaunch update(final UserLaunch userLaunch) {
        UserLaunch updateUserLaunch = getById(userLaunch.getId());
        if (updateUserLaunch != null) {
            updateUserLaunch.setLastLaunch(new Timestamp(new Date().getTime()));
            return save(updateUserLaunch);
        }
        return null;
    }

    @Override
    public UserLaunch findByUserIdAndLaunchScenarioId(String ldapId, int launchScenarioId) {
        return repository.findByUserIdAndLaunchScenarioId(ldapId, launchScenarioId);
    }

    @Override
    public List<UserLaunch> findByUserId(String ldapId) {
        return repository.findByUserId(ldapId);
    }

    @Override
    public List<UserLaunch> findByLaunchScenarioId(int launchScenarioId) {
        return repository.findByLaunchScenarioId(launchScenarioId);
    }

}
