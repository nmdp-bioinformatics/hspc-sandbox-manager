package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.TermsOfUse;

import java.util.List;

public interface TermsOfUseService {

    TermsOfUse save(final TermsOfUse termsOfUse);

    TermsOfUse getById(final int id);

    List<TermsOfUse> orderByCreatedTimestamp();

}

