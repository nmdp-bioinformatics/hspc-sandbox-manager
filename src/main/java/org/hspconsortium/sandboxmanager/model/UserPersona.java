package org.hspconsortium.sandboxmanager.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@NamedQueries({
        @NamedQuery(name="UserPersona.findByLdapId",
                query="SELECT c FROM UserPersona c WHERE c.ldapId = :ldapId"),
        @NamedQuery(name="UserPersona.findByFhirIdAndSandboxId",
                query="SELECT c FROM UserPersona c WHERE c.fhirId = :fhirId and c.sandbox.sandboxId = :sandboxId"),
        @NamedQuery(name="UserPersona.findBySandboxId",
                query="SELECT c FROM UserPersona c WHERE c.sandbox.sandboxId = :sandboxId"),
        @NamedQuery(name="UserPersona.findBySandboxIdAndCreatedByOrVisibility",
                query="SELECT c FROM UserPersona c WHERE c.sandbox.sandboxId = :sandboxId and " +
                "(c.createdBy.ldapId = :createdBy or c.visibility = :visibility)"),
        @NamedQuery(name="UserPersona.findBySandboxIdAndCreatedBy",
                query="SELECT c FROM UserPersona c WHERE c.sandbox.sandboxId = :sandboxId and " +
                        "c.createdBy.ldapId = :createdBy")
})
public class UserPersona extends AbstractSandboxItem {
    private String ldapId;
    private String ldapName;
    private String password;
    private String fhirId;
    private String fhirName;
    private String resource;
    private String resourceUrl;

    /******************* User Persona Property Getter/Setters ************************/

    public String getLdapId() {
        return ldapId;
    }

    public void setLdapId(String ldapId) {
        this.ldapId = ldapId;
    }

    public String getLdapName() {
        return ldapName;
    }

    public void setLdapName(String ldapName) {

        this.ldapName = ldapName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFhirId() {
        return fhirId;
    }

    public void setFhirId(String fhirId) {
        this.fhirId = fhirId;
    }

    public String getFhirName() {
        return fhirName;
    }

    public void setFhirName(String fhirName) {
        this.fhirName = fhirName;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
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

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="sandbox_id")
    public Sandbox getSandbox() {
        return sandbox;
    }

    public void setSandbox(Sandbox sandbox) {
        this.sandbox = sandbox;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

}
