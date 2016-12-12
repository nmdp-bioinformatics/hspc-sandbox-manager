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
        query="SELECT c FROM App c WHERE c.sandbox.sandboxId = :sandboxId and c.authClient.authDatabaseId IS NOT NULL"),
        @NamedQuery(name="App.findBySandboxIdAndCreatedByOrVisibility",
        query="SELECT c FROM App c WHERE c.sandbox.sandboxId = :sandboxId and c.authClient.authDatabaseId IS NOT NULL and " +
                "(c.createdBy.ldapId = :createdBy or c.visibility = :visibility)"),
        @NamedQuery(name="App.findBySandboxIdAndCreatedBy",
        query="SELECT c FROM App c WHERE c.sandbox.sandboxId = :sandboxId and c.authClient.authDatabaseId IS NOT NULL and " +
                "c.createdBy.ldapId = :createdBy")
})
public class App extends AbstractSandboxItem {

    private String launchUri;
    private String logoUri;
    private Image logo;
    private AuthClient authClient;
    private String samplePatients;
    private String clientJSON;

    /******************* App Property Getter/Setters ************************/

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
    @JoinColumn(name="auth_client_id")
    public AuthClient getAuthClient() {
        return authClient;
    }

    public void setAuthClient(AuthClient authClient) {
        this.authClient = authClient;
    }

    public String getSamplePatients() {
        return samplePatients;
    }

    public void setSamplePatients(String samplePatients) {
        this.samplePatients = samplePatients;
    }

    @Transient
    public String getClientJSON() {
        return clientJSON;
    }

    public void setClientJSON(String clientJSON) {
        this.clientJSON = clientJSON;
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
