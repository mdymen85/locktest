package com.mdymen85.locktest;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Transactional
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @PostMapping("/users/random")
    public void createRandom() {
        for (int i = 0; i < 100000; i++) {
            User user = new User();
            user.setName("User " + i);
            user.setExternalId("external-" + i);
            user.setActivated(true);
            // Assume userRepository is injected and available
            userRepository.saveAndFlush(user);
            log.info("Created user with externalId: {}", user.getExternalId());
        }
    }

    @DeleteMapping("/users")
    public void deleteAll() throws InterruptedException {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            user.setActivated(false);
            userRepository.saveAndFlush(user);
            log.info("Deactivated user with externalId: {}", user.getExternalId());
        }
        log.info("All users deactivated. Sleeping for 50 seconds...");
        Thread.sleep(50000); // Simulate some delay
    }

    @DeleteMapping("/users/{externalId}")
    public ResponseEntity<User> deleteByExternalId(@PathVariable String externalId) {
        log.info("Deactivating user with externalId: {}", externalId);
        User user = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActivated(false);
        log.info("User with externalId: {} deactivated", externalId);
        userRepository.saveAndFlush(user);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/all-flush")
    public void deleteAllFlush() throws InterruptedException {
        userRepository.updateAll();
        Thread.sleep(50000); // Simulate some delay
        log.info("All users deactivated via bulk update.");
    }

    @DeleteMapping("/users/all-flush/executor")
    @Transactional
    public void deleteAllFlushExecutor() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) { // 10 tasks
            executor.submit(() -> {
                try {
                    userService.deactivateAllUsers();
                    Thread.sleep(50000);
                } catch (Exception e) {
                    log.error("Error during bulk update", e);
                }
            });
        }

// Wait for all tasks to finish
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);


    }



    @PostMapping("/test/concurrent-deletes")
    public void testConcurrentDeletes() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Task 1: deleteAll
        executor.submit(() -> {
            userRepository.findAll().forEach(user -> {
                user.setActivated(false);
                userRepository.saveAndFlush(user);
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            });
            System.out.println("Task 1 finished");
        });

        // Task 2: delete a single user
        executor.submit(() -> {
            try { Thread.sleep(50); } catch (InterruptedException ignored) {} // small delay
            userRepository.findByExternalId("external-10").ifPresent(user -> {
                user.setActivated(false);
                userRepository.saveAndFlush(user);
            });
            System.out.println("Task 2 finished");
        });

        executor.shutdown();
        while (!executor.isTerminated()) { Thread.sleep(100); }
    }

    @DeleteMapping("/users/parallel/{externalId}")
    public void deleteByExternalIdParallel(@PathVariable String externalId, @RequestParam boolean heldOpen) throws InterruptedException {
        userService.deactivateUserByExternalId(externalId, heldOpen);
    }

}
