package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.SnapshotAction;

import java.io.UnsupportedEncodingException;

/**
 */
public interface DataManagerService {

    String importData(final Sandbox sandbox, final String bearerToken, final String count) throws UnsupportedEncodingException;

    String reset(final Sandbox sandbox, final String bearerToken) throws UnsupportedEncodingException;
}
