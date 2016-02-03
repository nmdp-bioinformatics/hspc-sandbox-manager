package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.User;
import org.hspconsortium.sandboxmanager.repositories.UserRepository;
import org.hspconsortium.sandboxmanager.services.UserService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Inject
    public UserServiceImpl(final UserRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public User save(final User user) {
        return repository.save(user);
    }

    public User findByLdapId(final String ldapId) {
        return repository.findByLdapId(ldapId);
    }

}


