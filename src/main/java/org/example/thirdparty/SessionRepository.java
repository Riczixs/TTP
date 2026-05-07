package org.example.thirdparty;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends CrudRepository<Session, UUID> {
    Optional<Session> findByPart1(UUID part1);
}
