package com.example.weeklymealplannergpt.service.consumer;

import com.example.weeklymealplannergpt.exception.ConsumerNotFoundException;
import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.repository.ConsumerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConsumerServiceImpl implements ConsumerService {

    private final ConsumerRepository consumerRepository;

    public ConsumerServiceImpl(ConsumerRepository consumerRepository) {
        this.consumerRepository = consumerRepository;
    }

    @Override
    public Consumer save(Consumer consumer) {
        validateEmail(consumer.getEmail());
        if (consumerRepository.findByEmail(consumer.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Consumer already exists");
        }
        return consumerRepository.save(consumer);
    }

    @Override
    public Consumer findByEmail(String email) {
        return consumerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Consumer not found"));
    }

    @Override
    public Optional<Consumer> findById(UUID id) {
        return consumerRepository.findById(id);
    }

    @Override
    public List<Consumer> findAll() {
        return consumerRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        if (!consumerRepository.existsById(id)) {
            throw new ConsumerNotFoundException("Consumer not found");
        }
        consumerRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return consumerRepository.existsById(id);
    }

    private void validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email can not be null or empty");
        }
    }
}
