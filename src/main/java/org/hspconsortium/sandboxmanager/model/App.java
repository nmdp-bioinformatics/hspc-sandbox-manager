package org.hspconsortium.sandboxmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@NamedQueries({
        @NamedQuery(name="App.findByLaunchUriAndClientIdAndSandboxId",
        query="SELECT c FROM App c WHERE c.launchUri = :launchUri and " +
                "c.authClient.clientId = :clientId and c.sandbox.sandboxId = :sandboxId"),
        @NamedQuery(name="App.findBySandboxId",
        query="SELECT c FROM App c WHERE c.sandbox.sandboxId = :sandboxId and c.authClient.authDatabaseId IS NOT NULL")
})
public class App {
    private Integer id;
    private Timestamp createdTimestamp;
    private String launchUri;
    private Image logo;
    private String logoUri;
    private AuthClient authClient;
    private String clientJSON;
    private Sandbox sandbox;

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

    public void setLaunchUri(String launchUri) {
        this.launchUri = launchUri;
    }

    public String getLaunchUri() {
        return launchUri;
    }

    public String getLogoUri() {
        return logoUri;
    }

    public void setLogoUri(String logoUri) {
        this.logoUri = logoUri;
    }

    @OneToOne(cascade={CascadeType.ALL})
    @JoinColumn(name="logo_id")
    @JsonIgnore
    public Image getLogo() {
        return logo;
    }

    public void setLogo(Image logo) {
        this.logo = logo;
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
    @JoinColumn(name="auth_client_id")
    public AuthClient getAuthClient() {
        return authClient;
    }

    public void setAuthClient(AuthClient authClient) {
        this.authClient = authClient;
    }

    @Transient
    public String getClientJSON() {
        return clientJSON;
    }

    public void setClientJSON(String clientJSON) {
        this.clientJSON = clientJSON;
    }

}
