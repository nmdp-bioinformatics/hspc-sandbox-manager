package org.hspconsortium.sandboxmanager.services;

import org.hspconsortium.sandboxmanager.model.Image;

public interface ImageService {

    Image save(final Image image);

    void delete(final int id);
}
