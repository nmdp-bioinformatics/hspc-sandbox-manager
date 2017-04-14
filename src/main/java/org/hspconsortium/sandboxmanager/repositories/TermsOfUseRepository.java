package org.hspconsortium.sandboxmanager.repositories;

import org.hspconsortium.sandboxmanager.model.TermsOfUse;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TermsOfUseRepository extends CrudRepository<TermsOfUse, Integer> {
    public List<TermsOfUse> orderByCreatedTimestamp();
}
