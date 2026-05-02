package com.medj.service.impl;

import com.medj.entities.MedicalSummary;
import com.medj.repositories.IMedicalSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class SummaryStore {

    public record SummaryEntry(String username, String summary, LocalDateTime generatedAt) {}

    @Autowired
    private IMedicalSummaryRepository repository;

    public String save(String username, String summary) {
        String token = UUID.randomUUID().toString();

        MedicalSummary entity = new MedicalSummary();
        entity.setToken(token);
        entity.setUsername(username);
        entity.setSummary(summary);
        entity.setGeneratedAt(LocalDateTime.now());
        entity.setIsActive(true);

        repository.save(entity);
        return token;
    }

    public Optional<SummaryEntry> find(String token) {
        return repository.findByToken(token)
                .map(e -> new SummaryEntry(e.getUsername(), e.getSummary(), e.getGeneratedAt()));
    }
}
