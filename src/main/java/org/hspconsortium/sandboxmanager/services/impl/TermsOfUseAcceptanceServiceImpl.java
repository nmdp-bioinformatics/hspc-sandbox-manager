package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.TermsOfUseAcceptance;
import org.hspconsortium.sandboxmanager.repositories.TermsOfUseAcceptanceRepository;
import org.hspconsortium.sandboxmanager.services.TermsOfUseAcceptanceService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
public class TermsOfUseAcceptanceServiceImpl implements TermsOfUseAcceptanceService {

    private final TermsOfUseAcceptanceRepository repository;

    @Inject
    public TermsOfUseAcceptanceServiceImpl(final TermsOfUseAcceptanceRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public TermsOfUseAcceptance save(TermsOfUseAcceptance termsOfUseAcceptance) {
        return repository.save(termsOfUseAcceptance);
    }

    @Override
    public TermsOfUseAcceptance getById(final int id) {
        return  repository.findOne(id);
    }

}
