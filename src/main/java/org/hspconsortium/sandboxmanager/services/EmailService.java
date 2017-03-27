package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.User;

import java.io.IOException;

public interface EmailService {

    void sendEmail(final User inviter, final User invitee, Sandbox sandbox) throws IOException;

}
