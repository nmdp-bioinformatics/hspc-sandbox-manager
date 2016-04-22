package org.hspconsortium.sandboxmanager.model;

import javax.persistence.*;
import java.math.BigInteger;

@Entity
@NamedQueries({
        @NamedQuery(name="App.findByLaunchUri",
        query="SELECT c FROM App c WHERE c.launch_uri = :uri and c.sandbox is null"),
        @NamedQuery(name="App.findByClientId",
        query="SELECT c FROM App c WHERE c.client_id = :client_id and c.sandbox is null"),
        @NamedQuery(name="App.findByLaunchUriAndSandboxId",
        query="SELECT c FROM App c WHERE c.launch_uri = :uri and c.sandbox.sandboxId = :sandboxId"),
        @NamedQuery(name="App.findByClientIdAndSandboxId",
        query="SELECT c FROM App c WHERE c.client_id = :client_id and c.sandbox.sandboxId = :sandboxId")

})
public class App {
    private Integer id;
    private String client_name;
    private String client_id;
    private String launch_uri;
    private String logo_uri;
    private Sandbox sandbox;

    public void setId(Integer id) {
        this.id = id;
    }

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public String getClient_name() {
        return client_name;
    }


    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public void setLaunch_uri(String launch_uri) {
        this.launch_uri = launch_uri;
    }

    public String getLaunch_uri() {
        return launch_uri;
    }

    public String getLogo_uri() {
        return logo_uri;
    }

    public void setLogo_uri(String logo_uri) {
        this.logo_uri = logo_uri;
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
