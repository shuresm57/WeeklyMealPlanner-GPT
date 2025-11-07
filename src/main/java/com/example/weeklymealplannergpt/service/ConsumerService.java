package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.model.Consumer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsumerService {
    Consumer save(Consumer consumer);
    Optional<Consumer> findById(UUID id);
    Optional<Consumer> findByEmail(String email);
    List<Consumer> findAll();
    void deleteById(UUID id);
    boolean existsById(UUID id);
}
