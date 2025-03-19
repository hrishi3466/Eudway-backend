package com.eduway.auth.repository;

import com.eduway.auth.model.User;
import com.eduway.auth.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUser(User user);

    void deleteByUser(User user);
}
