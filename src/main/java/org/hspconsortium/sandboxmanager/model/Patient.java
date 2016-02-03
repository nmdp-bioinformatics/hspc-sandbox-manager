package org.hspconsortium.sandboxmanager.model;

import javax.persistence.*;
import java.math.BigInteger;

@Entity
@NamedQueries({
        @NamedQuery(name="Patient.findByFhirId",
                query="SELECT c FROM Patient c WHERE c.fhirId = :id")
})
public class Patient {
    private Integer id;
    private String name;
    private String fhirId;
    private String resource;

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
}
