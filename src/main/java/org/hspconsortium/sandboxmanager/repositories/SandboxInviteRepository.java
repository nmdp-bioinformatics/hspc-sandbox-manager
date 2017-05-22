package org.hspconsortium.sandboxmanager.repositories;

import org.hspconsortium.sandboxmanager.model.SandboxInvite;
import org.hspconsortium.sandboxmanager.model.InviteStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SandboxInviteRepository extends CrudRepository<SandboxInvite, Integer> {
    public List<SandboxInvite> findInvitesByInviteeId(@Param("inviteeId") String inviteeId);
    public List<SandboxInvite> findInvitesBySandboxId(@Param("sandboxId") String sandboxId);
    public List<SandboxInvite> findInvitesByInviteeIdAndSandboxId(@Param("inviteeId") String inviteeId, @Param("sandboxId") String sandboxId);
    public List<SandboxInvite> findInvitesByInviteeEmailAndSandboxId(@Param("inviteeEmail") String inviteeEmail, @Param("sandboxId") String sandboxId);
    public List<SandboxInvite> findInvitesByInviteeIdAndStatus(@Param("inviteeId") String inviteeId, @Param("status") InviteStatus status);
    public List<SandboxInvite> findInvitesBySandboxIdAndStatus(@Param("sandboxId") String sandboxId, @Param("status") InviteStatus status);
}
