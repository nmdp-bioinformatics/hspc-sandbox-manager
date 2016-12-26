package org.hspconsortium.sandboxmanager.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@NamedQueries({
        @NamedQuery(name="SandboxActivityLog.findByUserLdapId",
                query="SELECT c FROM SandboxActivityLog c WHERE c.user.ldapId = :ldapId"),
        @NamedQuery(name="SandboxActivityLog.findBySandboxId",
                query="SELECT c FROM SandboxActivityLog c WHERE c.sandbox.sandboxId = :sandboxId"),
        @NamedQuery(name="SandboxActivityLog.findBySandboxActivity",
                query="SELECT c FROM SandboxActivityLog c WHERE c.activity = :sandboxActivity")
})
public class SandboxActivityLog {
    private Integer id;
    private Timestamp timestamp;
    private User user;
    private SandboxActivity activity;
    private Sandbox sandbox;
    private String additionalInfo;

    public void setId(Integer id) {
        this.id = id;
    }

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="user_id")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setActivity(SandboxActivity activity) {

        this.activity = activity;
    }

    public SandboxActivity getActivity() {
        return activity;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="sandbox_id")
    public Sandbox getSandbox() {
        return sandbox;
    }

    public void setSandbox(Sandbox sandbox) {
        this.sandbox = sandbox;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}