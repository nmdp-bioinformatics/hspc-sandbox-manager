package org.hspconsortium.sandboxmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.lang.annotation.Target;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@NamedQueries({
        @NamedQuery(name="LaunchScenario.findBySandboxId",
                query="SELECT c FROM LaunchScenario c WHERE c.sandbox.sandboxId = :sandboxId"),
        @NamedQuery(name="LaunchScenario.findByAppIdAndSandboxId",
                query="SELECT c FROM LaunchScenario c WHERE c.app.id = :appId and c.sandbox.sandboxId = :sandboxId"),
        @NamedQuery(name="LaunchScenario.findByUserPersonaIdAndSandboxId",
               query="SELECT c FROM LaunchScenario c WHERE c.userPersona.id = :userPersonaId and c.sandbox.sandboxId = :sandboxId")
})
public class LaunchScenario {

    private Integer id;
    private String description;
    private User createdBy;
    private List<User> users = new ArrayList<>();
    private List<String> userIds = new ArrayList<>();
    private Patient patient;
    private Persona persona;
    private UserPersona userPersona;
    private App app;
    private Sandbox sandbox;
    private List<ContextParams> contextParams;
    private Timestamp lastLaunch;
    private Long lastLaunchSeconds;

    public void setId(Integer id) {
        this.id = id;
    }

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="created_by_id")
    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "user_launch_scenario", joinColumns = {
            @JoinColumn(name = "launch_scenario_id", nullable = false, updatable = false) },
            inverseJoinColumns = { @JoinColumn(name = "user_id",
                    nullable = false, updatable = false) })
    @JsonIgnore
    public List<User> getUsers() {
        for (User user : users) {
            userIds.add(user.getLdapId());
        }
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    @Transient
    public List<String> getUserIds() {
        return this.userIds;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="sandbox_id")
    public Sandbox getSandbox() {
        return sandbox;
    }

    public void setSandbox(Sandbox sandbox) {
        this.sandbox = sandbox;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="patient_id")
    public Patient getPatient() {
        return patient;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="persona_id")
    public Persona getPersona() {
        return persona;
    }

    public void setUserPersona(UserPersona userPersona) {
        this.userPersona = userPersona;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="user_persona_id")
    public UserPersona getUserPersona() {
        return userPersona;
    }

    public void setApp(App app) {
        this.app = app;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="app_id")
    public App getApp() {
        return app;
    }

    public void setContextParams(List<ContextParams> contextParams) {
        this.contextParams = contextParams;
    }

    @OneToMany(cascade={CascadeType.ALL})
    public List<ContextParams> getContextParams() {
        return contextParams;
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
