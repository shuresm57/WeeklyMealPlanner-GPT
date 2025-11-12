package com.example.weeklymealplannergpt.repository;

import com.example.weeklymealplannergpt.model.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConsumerRepository extends JpaRepository<Consumer, UUID> {
    Optional<Consumer> findByEmail(String email);
}
