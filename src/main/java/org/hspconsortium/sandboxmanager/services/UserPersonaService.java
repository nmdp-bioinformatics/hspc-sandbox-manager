package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.UserPersona;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface UserPersonaService {

    UserPersona save(final UserPersona userPersona);

    UserPersona getById(final int id);

    UserPersona findByLdapId(final String ldapId);

    UserPersona findByFhirIdAndSandboxId(final String fhirId, final String sandboxId);

    List<UserPersona> findBySandboxId(final String sandboxId);

    void delete(final int id);

    void delete(final UserPersona userPersona, final String bearerToken);

    UserPersona create(final UserPersona userPersona, final String bearerToken) throws UnsupportedEncodingException;

    UserPersona update(final UserPersona userPersona, final String bearerToken) throws UnsupportedEncodingException;

}
