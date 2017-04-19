package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.SandboxImport;

public interface SandboxImportService {

    SandboxImport save(final SandboxImport sandboxImport);

    void delete(final int id);

    void delete(SandboxImport sandboxImport);

}
