package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.User;
import org.hspconsortium.sandboxmanager.model.UserRole;

import java.io.UnsupportedEncodingException;

public interface SandboxService {

    Sandbox save(final Sandbox sandbox);

    void delete(final int id);

    void delete(final Sandbox sandbox, final String bearerToken);

    Sandbox create(final Sandbox sandbox, final User user, final String bearerToken) throws UnsupportedEncodingException;

    Sandbox update(final Sandbox sandbox);

    void removeMember(final Sandbox sandbox, final User user);

    void removeAllMembers(final Sandbox sandbox);

    void addMember(final Sandbox sandbox, final User user);

    boolean isSandboxMember(final Sandbox sandbox, final User user);

    Sandbox findBySandboxId(final String sandboxId);
}
