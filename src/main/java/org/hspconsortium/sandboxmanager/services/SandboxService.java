package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.*;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.List;

public interface SandboxService {

    Sandbox save(final Sandbox sandbox);

    void delete(final int id);

    void delete(final Sandbox sandbox, final String bearerToken);

    Sandbox create(final Sandbox sandbox, final User user, final String bearerToken) throws UnsupportedEncodingException;

    Sandbox update(final Sandbox sandbox, final User user, final String bearerToken) throws UnsupportedEncodingException;

    void removeMember(final Sandbox sandbox, final User user, final String bearerToken);

    void addMember(final Sandbox sandbox, final User user);

    void addMember(final Sandbox sandbox, final User user, final Role role);

    void addMemberRole(final Sandbox sandbox, final User user, final Role role);

    boolean hasMemberRole(final Sandbox sandbox, final User user, final Role role);

    void sandboxLogin(final String sandboxId, final String userId);

    boolean isSandboxMember(final Sandbox sandbox, final User user);

    List<Sandbox> getAllowedSandboxes(final User user);

    Sandbox findBySandboxId(final String sandboxId);

    List<Sandbox> findByVisibility(final Visibility visibility);

    String fullCount();

    String schemaCount(final String schemaVersion);

    String intervalCount(final Timestamp intervalTime);
}
