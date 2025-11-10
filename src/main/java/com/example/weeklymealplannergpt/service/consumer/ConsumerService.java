package com.example.weeklymealplannergpt.service.consumer;

import com.example.weeklymealplannergpt.model.Consumer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsumerService {
    Consumer save(Consumer consumer);
    Consumer findByEmail(String email);
    Optional<Consumer> findById(UUID id);
    List<Consumer> findAll();
    void deleteById(UUID id);
    boolean existsById(UUID id);
}
