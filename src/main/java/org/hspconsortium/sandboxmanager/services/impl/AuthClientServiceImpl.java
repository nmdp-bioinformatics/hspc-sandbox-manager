package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.AuthClient;
import org.hspconsortium.sandboxmanager.repositories.AuthClientRepository;
import org.hspconsortium.sandboxmanager.services.AuthClientService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
public class AuthClientServiceImpl implements AuthClientService {

    private final AuthClientRepository repository;

    @Inject
    public AuthClientServiceImpl(final AuthClientRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public AuthClient save(final AuthClient authClient) {
        return repository.save(authClient);
    }

    @Override
    @Transactional
    public void delete(final int id) {
        repository.delete(id);
    }

}
