package org.hspconsortium.sandboxmanager.model;

import javax.persistence.*;
import java.math.BigInteger;

@Entity
@NamedQueries({
        @NamedQuery(name="Persona.findByFhirIdAndSandboxId",
                query="SELECT c FROM Persona c WHERE c.fhirId = :fhirId and c.sandbox.sandboxId = :sandboxId"),
        @NamedQuery(name="Persona.findBySandboxId",
                query="SELECT c FROM Persona c WHERE c.sandbox.sandboxId = :sandboxId")
})
public class Persona {
    private Integer id;
    private String name;
    private String fhirId;
    private String resource;
    private String fullUrl;
    private Sandbox sandbox;

    public void setId(Integer id) {
        this.id = id;
    }

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public String getFullUrl() {
        return fullUrl;
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
