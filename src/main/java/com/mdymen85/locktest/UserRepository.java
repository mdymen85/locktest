package com.mdymen85.locktest;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByExternalId(String externalId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.activated = false")
    void updateAll();
}
