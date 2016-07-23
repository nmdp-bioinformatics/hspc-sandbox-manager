package org.hspconsortium.sandboxmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@NamedQueries({
        @NamedQuery(name="SandboxInvite.findInvitesByInviteeId",
                query="SELECT c FROM SandboxInvite c WHERE c.invitee.ldapId = :inviteeId"),
        @NamedQuery(name="SandboxInvite.findInvitesBySandboxId",
                query="SELECT c FROM SandboxInvite c WHERE c.sandbox.sandboxId = :sandboxId"),
        @NamedQuery(name="SandboxInvite.findInvitesByInviteeIdAndSandboxId",
                query="SELECT c FROM SandboxInvite c WHERE c.invitee.ldapId = :inviteeId and c.sandbox.sandboxId = :sandboxId"),
        @NamedQuery(name="SandboxInvite.findInvitesByInviteeIdAndStatus",
                query="SELECT c FROM SandboxInvite c WHERE c.invitee.ldapId = :inviteeId and c.status = :status"),
        @NamedQuery(name="SandboxInvite.findInvitesBySandboxIdAndStatus",
                query="SELECT c FROM SandboxInvite c WHERE c.sandbox.sandboxId = :sandboxId and c.status = :status")
})
public class SandboxInvite {
    private Integer id;
    private User invitee;
    private User invitedBy;
    private Sandbox sandbox;
    private Timestamp inviteTimestamp;
    private InviteStatus status;

    public void setId(Integer id) {
        this.id = id;
    }

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="invitee_id")
    public User getInvitee() {
        return invitee;
    }

    public void setInvitee(User invitee) {
        this.invitee = invitee;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="invited_by_id")
    public User getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(User invitedBy) {
        this.invitedBy = invitedBy;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="sandbox_id")
    public Sandbox getSandbox() {
        return sandbox;
    }

    public void setSandbox(Sandbox sandbox) {
        this.sandbox = sandbox;
    }

    @JsonIgnore
    public Timestamp getInviteTimestamp() {
        return inviteTimestamp;
    }

    public void setInviteTimestamp(Timestamp inviteTimestamp) {
        this.inviteTimestamp = inviteTimestamp;
    }

    public void setStatus(InviteStatus status) {
        this.status = status;
    }

    public InviteStatus getStatus() {
        return status;
    }
}
