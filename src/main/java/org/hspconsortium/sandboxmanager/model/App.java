package org.hspconsortium.sandboxmanager.model;

import javax.persistence.*;
import java.math.BigInteger;

@Entity
@NamedQueries({
        @NamedQuery(name="App.findByLaunchUri",
                query="SELECT c FROM App c WHERE c.launch_uri = :uri")
})
public class App {
    private BigInteger id;
    private String client_name;
    private String launch_uri;

    public void setId(BigInteger id) {
        this.id = id;
    }

    @Id // @Id indicates that this it a unique primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public BigInteger getId() {
        return id;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public String getClient_name() {
        return client_name;
    }

    public void setLaunch_uri(String launch_uri) {
        this.launch_uri = launch_uri;
    }

    public String getLaunch_uri() {
        return launch_uri;
    }

}
