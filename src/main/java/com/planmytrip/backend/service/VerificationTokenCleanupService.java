package com.planmytrip.backend.service;

import com.planmytrip.backend.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VerificationTokenCleanupService {

    private final VerificationTokenRepository verificationTokenRepository;

    @Autowired
    public VerificationTokenCleanupService(VerificationTokenRepository verificationTokenRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @Scheduled(cron = "0 0 1 * * ?") // Runs at 1:00 AM every day
    @Transactional
    public void cleanupExpiredTokens() {
        verificationTokenRepository.deleteAllExpiredSince(LocalDateTime.now());
    }
}
