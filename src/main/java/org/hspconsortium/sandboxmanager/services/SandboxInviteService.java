package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.InviteStatus;
import org.hspconsortium.sandboxmanager.model.SandboxInvite;

import java.io.IOException;
import java.util.List;

public interface SandboxInviteService {

    SandboxInvite save(final SandboxInvite sandboxInvite);

    void delete(final int id);

    void delete(SandboxInvite sandboxInvite);

    SandboxInvite create(SandboxInvite sandboxInvite) throws IOException;

    SandboxInvite getById(final int id);

    List<SandboxInvite> findInvitesByInviteeId(final String inviteeId);

    List<SandboxInvite> findInvitesBySandboxId(final String sandboxId);

    List<SandboxInvite> findInvitesByInviteeIdAndSandboxId(final String inviteeId, final String sandboxId);

    List<SandboxInvite> findInvitesByInviteeIdAndStatus(final String inviteeId, final InviteStatus status);

    List<SandboxInvite> findInvitesBySandboxIdAndStatus(final String sandboxId, final InviteStatus status);
}
