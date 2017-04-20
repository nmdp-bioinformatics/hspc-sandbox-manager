package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.UserPersona;
import org.hspconsortium.sandboxmanager.model.Visibility;

import java.util.List;

public interface UserPersonaService {

    UserPersona save(final UserPersona userPersona);

    UserPersona getById(final int id);

    void delete(final int id);

    void delete(final UserPersona userPersona);

    UserPersona findByLdapId(final String ldapId);

    UserPersona findByLdapIdAndSandboxId(final String ldapId, final String sandboxId);

    List<UserPersona> findBySandboxId(final String sandboxId);

    UserPersona create(final UserPersona userPersona);

    UserPersona update(final UserPersona userPersona);

    List<UserPersona> findBySandboxIdAndCreatedByOrVisibility(String sandboxId, String createdBy, Visibility visibility);

    List<UserPersona> findBySandboxIdAndCreatedBy(String sandboxId, String createdBy);
}
