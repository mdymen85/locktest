package com.mdymen85.locktest;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void deactivateAllUsers() {
        userRepository.updateAll();
        log.info("All users deactivated.");

    }

    @Transactional(propagation = REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public void deactivateUserByExternalId(String externalId, boolean heldOpen) {
        log.info("Deactivating user with externalId: {}", externalId);
        User user = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with externalId: " + externalId));
        user.setName(UUID.randomUUID().toString());
        userRepository.saveAndFlush(user);
        if (heldOpen) {
            log.info("Holding transaction open for user with externalId: {}", externalId);
            try {
                Thread.sleep(60000); // Simulate long-running transaction
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("Method finished for externalId: {}", externalId);

    }

}
