package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.UserRole;
import org.hspconsortium.sandboxmanager.repositories.UserRoleRepository;
import org.hspconsortium.sandboxmanager.services.UserRoleService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepository repository;

    @Inject
    public UserRoleServiceImpl(final UserRoleRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void delete(final int id){
        repository.delete(id);
    };

    @Override
    @Transactional
    public UserRole save(final UserRole userRole) {
        return repository.save(userRole);
    }

}
