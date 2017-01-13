package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.LaunchScenario;
import org.hspconsortium.sandboxmanager.model.UserPersona;
import org.hspconsortium.sandboxmanager.model.Visibility;
import org.springframework.data.repository.query.Param;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface UserPersonaService {

    UserPersona save(final UserPersona userPersona);

    UserPersona getById(final int id);

    void delete(final int id);

    void delete(final UserPersona userPersona, final String bearerToken);

    UserPersona findByLdapId(final String ldapId);

    UserPersona findByFhirIdAndSandboxId(final String fhirId, final String sandboxId);

    List<UserPersona> findBySandboxId(final String sandboxId);

    UserPersona create(final UserPersona userPersona, final String bearerToken) throws UnsupportedEncodingException;

    UserPersona update(final UserPersona userPersona, final String bearerToken) throws UnsupportedEncodingException;

    List<UserPersona> findBySandboxIdAndCreatedByOrVisibility(String sandboxId, String createdBy, Visibility visibility);

    List<UserPersona> findBySandboxIdAndCreatedBy(String sandboxId, String createdBy);
}
