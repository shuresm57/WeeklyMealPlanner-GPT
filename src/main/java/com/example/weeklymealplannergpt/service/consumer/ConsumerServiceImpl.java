package com.example.weeklymealplannergpt.service.consumer;

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
        consumerRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return consumerRepository.existsById(id);
    }
}
