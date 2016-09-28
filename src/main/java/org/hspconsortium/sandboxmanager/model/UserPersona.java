package org.hspconsortium.sandboxmanager.model;

import javax.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(name="UserPersona.findByLdapId",
                query="SELECT c FROM UserPersona c WHERE c.ldapId = :ldapId"),
        @NamedQuery(name="UserPersona.findByFhirIdAndSandboxId",
                query="SELECT c FROM UserPersona c WHERE c.fhirId = :fhirId and c.sandbox.sandboxId = :sandboxId"),
        @NamedQuery(name="UserPersona.findBySandboxId",
                query="SELECT c FROM UserPersona c WHERE c.sandbox.sandboxId = :sandboxId")
})
public class UserPersona {
    private Integer id;
    private String ldapId;
    private String ldapName;
    private String password;
    private String fhirId;
    private String fhirName;
    private String resource;
    private String resourceUrl;
    private Sandbox sandbox;

    public void setId(Integer id) {
        this.id = id;
    }

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public String getLdapId() {
        return ldapId;
    }

    public void setLdapId(String ldapId) {
        this.ldapId = ldapId;
    }

    public void setLdapName(String ldapName) {

        this.ldapName = ldapName;
    }

    public String getLdapName() {
        return ldapName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFhirName(String fhirName) {
        this.fhirName = fhirName;
    }

    public String getFhirName() {
        return fhirName;
    }

    public void setFhirId(String fhirId) {
        this.fhirId = fhirId;
    }

    public String getFhirId() {
        return fhirId;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="sandbox_id")
    public Sandbox getSandbox() {
        return sandbox;
    }

    public void setSandbox(Sandbox sandbox) {
        this.sandbox = sandbox;
    }

}
