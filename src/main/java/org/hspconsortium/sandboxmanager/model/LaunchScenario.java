package org.hspconsortium.sandboxmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.math.BigInteger;
import java.sql.Timestamp;

@Entity
public class LaunchScenario {

    private BigInteger id;
    private String description;
    private Patient patient;
    private Persona persona;
    private App app;
    private Timestamp lastLaunch;
    private Long lastLaunchSeconds;

    public void setId(BigInteger id) {
        this.id = id;
    }

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public BigInteger getId() {
        return id;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
//    @PrimaryKeyJoinColumn
    @JoinColumn(name="patient_id")
    public Patient getPatient() {
        return patient;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
//    @PrimaryKeyJoinColumn
    @JoinColumn(name="persona_id")
    public Persona getPersona() {
        return persona;
    }

    public void setApp(App app) {
        this.app = app;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
//    @PrimaryKeyJoinColumn
    @JoinColumn(name="app_id")
    public App getApp() {
        return app;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @JsonIgnore
    public Timestamp getLastLaunch() {
        return lastLaunch;
    }

    public void setLastLaunch(Timestamp lastLaunch) {
        this.lastLaunch = lastLaunch;
    }

    @Transient
    public Long getLastLaunchSeconds() {
        if (this.lastLaunch != null) {
            this.lastLaunchSeconds = this.lastLaunch.getTime();
        }
        return this.lastLaunchSeconds;
    }

    public void setLastLaunchSeconds(Long lastLaunchSeconds) {
        this.lastLaunchSeconds = lastLaunchSeconds;
        if (lastLaunchSeconds != null) {
            this.lastLaunch = new Timestamp(lastLaunchSeconds);
        }
    }
}
