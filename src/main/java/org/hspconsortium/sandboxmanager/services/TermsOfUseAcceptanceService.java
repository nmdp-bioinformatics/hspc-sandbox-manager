package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.TermsOfUseAcceptance;

public interface TermsOfUseAcceptanceService {

    TermsOfUseAcceptance save(final TermsOfUseAcceptance termsOfUseAcceptance);

    TermsOfUseAcceptance getById(final int id);

}

