package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.TermsOfUse;

public interface TermsOfUseService {

    TermsOfUse save(final TermsOfUse termsOfUse);

    TermsOfUse getById(final int id);

    TermsOfUse mostRecent();

}

