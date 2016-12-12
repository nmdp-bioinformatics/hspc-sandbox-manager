package org.hspconsortium.sandboxmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@NamedQueries({
        @NamedQuery(name="UserLaunch.findByUserIdAndLaunchScenarioId",
                query="SELECT c FROM UserLaunch c WHERE c.user.ldapId = :ldapId and c.launchScenario.id = :launchScenarioId"),
        @NamedQuery(name="UserLaunch.findByUserId",
                query="SELECT c FROM UserLaunch c WHERE c.user.ldapId = :ldapId"),
        @NamedQuery(name="UserLaunch.findByLaunchScenarioId",
                query="SELECT c FROM UserLaunch c WHERE c.launchScenario.id = :launchScenarioId")
})
public class UserLaunch {
    private Integer id;
    private User user;
    private LaunchScenario launchScenario;
    private Timestamp lastLaunch;
    private Long lastLaunchSeconds;

    public UserLaunch() {}

    public UserLaunch(User user, LaunchScenario launchScenario, Timestamp lastLaunch) {
        this.user = user;
        this.launchScenario = launchScenario;
        this.lastLaunch = lastLaunch;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="user_id")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    @ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="launch_scenario_id")
    public LaunchScenario getLaunchScenario() {
        return launchScenario;
    }

    public void setLaunchScenario(LaunchScenario launchScenario) {
        this.launchScenario = launchScenario;
    }
}
