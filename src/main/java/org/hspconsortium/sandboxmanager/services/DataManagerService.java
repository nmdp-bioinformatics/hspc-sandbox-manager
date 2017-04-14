package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.SnapshotAction;

import java.io.UnsupportedEncodingException;

/**
 */
public interface DataManagerService {

    String importPatientData(final Sandbox sandbox, final String bearerToken, final String endpoint, final String patientId, final String fhirIdPrefix) throws UnsupportedEncodingException;

    String reset(final Sandbox sandbox, final String bearerToken) throws UnsupportedEncodingException;
}
