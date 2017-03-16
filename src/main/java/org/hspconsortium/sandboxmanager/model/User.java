package org.hspconsortium.sandboxmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NamedQueries({
        // Used to retrieve a user instance for multiple uses
        @NamedQuery(name="User.findByLdapId",
                query="SELECT c FROM User c WHERE c.ldapId = :ldapId"),
        // Used for statistics
        @NamedQuery(name="User.fullCount",
                query="SELECT COUNT(*) FROM User c"),
        // Used for statistics
        @NamedQuery(name="User.intervalCount",
                query="SELECT COUNT(*) FROM User c WHERE c.createdTimestamp  >= :intervalTime")

})
public class User {
    private Integer id;
    private Timestamp createdTimestamp;
    private String ldapId;
    private String name;
    private Set<SystemRole> systemRoles = new HashSet<>();
    private List<Sandbox> sandboxes = new ArrayList<>();

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @ElementCollection(targetClass = SystemRole.class)
    @CollectionTable(name = "system_role",joinColumns = @JoinColumn(name = "user_id"))
    @Column(name ="role", nullable = false)
    public Set<SystemRole> getSystemRoles() {
        return systemRoles;
    }

    public void setSystemRoles(Set<SystemRole> systemRoles) {
        this.systemRoles = systemRoles;
    }

    @ManyToMany(cascade={CascadeType.ALL})
    @JoinTable(name = "user_sandbox", joinColumns = {
            @JoinColumn(name = "user_id", nullable = false, updatable = false) },
            inverseJoinColumns = { @JoinColumn(name = "sandbox_id",
                    nullable = false, updatable = false) })
    @JsonIgnore
    public List<Sandbox> getSandboxes() {
        return sandboxes;
    }

    public void setSandboxes(List<Sandbox> sandboxes) {
        this.sandboxes = sandboxes;
    }

}
