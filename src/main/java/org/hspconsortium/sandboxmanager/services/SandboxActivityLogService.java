package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface SandboxActivityLogService {

    SandboxActivityLog save(final SandboxActivityLog sandboxActivityLog);

    void delete(final SandboxActivityLog sandboxActivityLog);

    SandboxActivityLog sandboxCreate(final Sandbox sandbox, final User user);

    SandboxActivityLog sandboxLogin(final Sandbox sandbox, final User user);

    SandboxActivityLog sandboxDelete(final Sandbox sandbox, final User user);

    SandboxActivityLog sandboxUserInviteAccepted(final Sandbox sandbox, final User user);

    SandboxActivityLog sandboxUserInviteRevoked(final Sandbox sandbox, final User user);

    SandboxActivityLog sandboxUserInviteRejected(final Sandbox sandbox, final User user);

    SandboxActivityLog sandboxUserRemoved(final Sandbox sandbox, final User user, final User removedUser);

    SandboxActivityLog sandboxUserInvited(final Sandbox sandbox, final User user, final User invitedUser);

    SandboxActivityLog sandboxOpenEndpoint(final Sandbox sandbox, final User user, final Boolean openEndpoint);

    SandboxActivityLog sandboxUserAdded(final Sandbox sandbox, final User user);

    SandboxActivityLog sandboxUserRoleChange(final Sandbox sandbox, final User user, final Role role, final boolean roleAdded);

    SandboxActivityLog systemUserCreated(final Sandbox sandbox, final User user);

    SandboxActivityLog systemUserRoleChange(final Sandbox sandbox, final User user, final SystemRole systemRole, final boolean roleAdded);

    List<SandboxActivityLog> findBySandboxId(final String sandboxId);

    List<SandboxActivityLog> findByUserLdapId(final String ldapId);

    List<SandboxActivityLog> findBySandboxActivity(final SandboxActivity sandboxActivity);
}
