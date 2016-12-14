package org.hspconsortium.sandboxmanager.model;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@NamedQueries({
        @NamedQuery(name="Sandbox.findBySandboxId",
                query="SELECT c FROM Sandbox c WHERE c.sandboxId = :sandboxId"),
        @NamedQuery(name="Sandbox.findByVisibility",
                query="SELECT c FROM Sandbox c WHERE c.visibility = :visibility")

})
public class Sandbox extends AbstractItem {

    private String sandboxId;
    private String name;
    private String description;
    private String schemaVersion;
    private String fhirServerEndPoint;
    private boolean allowOpenAccess;
    private List<UserRole> userRoles = new ArrayList<>();

    /******************* Sandbox Property Getter/Setters ************************/

    public String getSandboxId() {
        return sandboxId;
    }

    public void setSandboxId(String sandboxId) {
        this.sandboxId = sandboxId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getFhirServerEndPoint() {
        return fhirServerEndPoint;
    }

    public void setFhirServerEndPoint(String fhirServerEndPoint) {
        this.fhirServerEndPoint = fhirServerEndPoint;
    }

    public boolean isAllowOpenAccess() {
        return allowOpenAccess;
    }

    public void setAllowOpenAccess(boolean allowOpenAccess) {
        this.allowOpenAccess = allowOpenAccess;
    }

    @OneToMany(cascade={CascadeType.ALL})
    @JoinTable(name = "sandbox_user_roles", joinColumns = {
            @JoinColumn(name = "sandbox", nullable = false, updatable = false) },
            inverseJoinColumns = { @JoinColumn(name = "user_roles",
                    nullable = false, updatable = false) })
    public List<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    /******************* Inherited Property Getter/Setters ************************/

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="created_by_id")
    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

}
