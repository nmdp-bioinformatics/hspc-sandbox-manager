package org.hspconsortium.sandboxmanager.services.impl;

import org.hspconsortium.sandboxmanager.model.SandboxInvite;
import org.hspconsortium.sandboxmanager.model.InviteStatus;
import org.hspconsortium.sandboxmanager.repositories.SandboxInviteRepository;
import org.hspconsortium.sandboxmanager.services.SandboxInviteService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class SandboxInviteServiceImpl implements SandboxInviteService {

    private final SandboxInviteRepository repository;

    @Inject
    public SandboxInviteServiceImpl(final SandboxInviteRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public SandboxInvite save(final SandboxInvite sandboxInvite) {
        return repository.save(sandboxInvite);
    }

    @Override
    public SandboxInvite getById(final int id) {
        return  repository.findOne(id);
    }

    @Override
    public List<SandboxInvite> findInvitesByInviteeId(final String inviteeId) {
        return repository.findInvitesByInviteeId(inviteeId);
    }

    @Override
    public List<SandboxInvite> findInvitesBySandboxId(final String sandboxId) {
        return repository.findInvitesBySandboxId(sandboxId);
    }

    @Override
    public List<SandboxInvite> findInvitesByInviteeIdAndSandboxId(final String inviteeId, final String sandboxId) {
        return repository.findInvitesByInviteeIdAndSandboxId(inviteeId, sandboxId);
    }

    @Override
    public List<SandboxInvite> findInvitesByInviteeIdAndStatus(final String inviteeId, final InviteStatus status) {
        return repository.findInvitesByInviteeIdAndStatus(inviteeId, status);
    }

    @Override
    public List<SandboxInvite> findInvitesBySandboxIdAndStatus(final String sandboxId, final InviteStatus status) {
        return repository.findInvitesBySandboxIdAndStatus(sandboxId, status);
    }
}


