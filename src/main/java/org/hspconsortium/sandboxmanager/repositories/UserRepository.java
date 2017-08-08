package org.hspconsortium.sandboxmanager.repositories;

import org.hspconsortium.sandboxmanager.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;

public interface UserRepository extends CrudRepository<User, Integer> {
    public User findBySbmUserId(@Param("sbmUserId") String sbmUserId);
    public User findByUserEmail(@Param("email") String email);
    public String fullCount();
    public String intervalCount(@Param("intervalTime") Timestamp intervalTime);
}
