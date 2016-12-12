package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.ContextParams;

public interface ContextParamsService {

    ContextParams save(final ContextParams contextParams);

    void delete(final int id);

    void delete(ContextParams contextParams);

}
