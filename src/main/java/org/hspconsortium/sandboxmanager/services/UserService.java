package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.User;

import java.sql.Timestamp;

public interface UserService {

    User save(final User user);

    void delete(final User user);

    User findBySbmUserId(final String sbmUserId);

    User findByUserEmail(final String email);

    String fullCount();

    String intervalCount(final Timestamp intervalTime);

    void removeSandbox(final Sandbox sandbox, final User user);

    void addSandbox(final Sandbox sandbox, final User user);

    boolean hasSandbox(final Sandbox sandbox, final User user);

    void acceptTermsOfUse(final User user, final String termsOfUseId);

}
