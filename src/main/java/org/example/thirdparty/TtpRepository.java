package org.example.thirdparty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TtpRepository extends CrudRepository<Client, UUID>{
    boolean existsByClientId(byte[] id);
    Optional<Client> findByClientId(byte [] id);
}
