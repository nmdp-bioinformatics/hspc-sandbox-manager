package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.User;
import org.hspconsortium.sandboxmanager.repositories.UserRepository;
import org.hspconsortium.sandboxmanager.services.UserService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

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

    @Override
    @Transactional
    public void removeSandbox(Sandbox sandbox, User user) {
        List<Sandbox> sandboxes = user.getSandboxes();
        sandboxes.remove(sandbox);
        user.setSandboxes(sandboxes);
        save(user);
    }

    @Override
    @Transactional
    public void addSandbox(Sandbox sandbox, User user) {
        List<Sandbox> sandboxes = user.getSandboxes();
        if (!sandboxes.contains(sandbox)) {
            sandboxes.add(sandbox);
            user.setSandboxes(sandboxes);
            save(user);
        }
    }

    @Override
    public boolean hasSandbox(Sandbox sandbox, User user) {
        return user.getSandboxes().contains(sandbox);
    }

}


