package org.hspconsortium.sandboxmanager.model;

import javax.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(name="User.findByLdapId",
                query="SELECT c FROM User c WHERE c.ldapId = :ldapId")
})
public class User {
    private Integer id;
    private String ldapId;
    private String name;

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

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
