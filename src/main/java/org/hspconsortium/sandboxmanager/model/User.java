package org.hspconsortium.sandboxmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@NamedQueries({
        @NamedQuery(name="User.findByLdapId",
                query="SELECT c FROM User c WHERE c.ldapId = :ldapId")
})
public class User {
    private Integer id;
    private Timestamp createdTimestamp;
    private String ldapId;
    private String name;
    private List<Sandbox> sandboxes = new ArrayList<>();
//    private List<String> sandboxIds = new ArrayList<>();

    public void setId(Integer id) {
        this.id = id;
    }

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getLdapId() {
        return ldapId;
    }

    public void setLdapId(String ldapId) {
        this.ldapId = ldapId;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getName() {
        return name;
    }

    @ManyToMany(cascade={CascadeType.ALL})
    @JoinTable(name = "user_sandbox", joinColumns = {
            @JoinColumn(name = "user_id", nullable = false, updatable = false) },
            inverseJoinColumns = { @JoinColumn(name = "sandbox_id",
                    nullable = false, updatable = false) })
    @JsonIgnore
    public List<Sandbox> getSandboxes() {
//        for (Sandbox sandbox : sandboxes) {
//            sandboxIds.add(sandbox.getSandboxId());
//        }

        return sandboxes;
    }

    public void setSandboxes(List<Sandbox> sandboxes) {
        this.sandboxes = sandboxes;
    }

//    @Transient
//    public List<String> getSandboxIds() {
//        return this.sandboxIds;
//    }

}
