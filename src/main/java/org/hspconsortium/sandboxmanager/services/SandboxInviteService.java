package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.SandboxInvite;
import org.hspconsortium.sandboxmanager.model.InviteStatus;

import java.util.List;

public interface SandboxInviteService {

    SandboxInvite save(final SandboxInvite sandboxInvite);

    SandboxInvite getById(final int id);

    List<SandboxInvite> findInvitesByInviteeId(final String inviteeId);

    List<SandboxInvite> findInvitesBySandboxId(final String sandboxId);

    List<SandboxInvite> findInvitesByInviteeIdAndSandboxId(final String inviteeId, final String sandboxId);

    List<SandboxInvite> findInvitesByInviteeIdAndStatus(final String inviteeId, final InviteStatus status);

    List<SandboxInvite> findInvitesBySandboxIdAndStatus(final String sandboxId, final InviteStatus status);
}
