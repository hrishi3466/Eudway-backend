package com.eduway.auth.repository;

import com.eduway.auth.model.UserBadge;
import com.eduway.auth.model.UserProfile;
import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    // Removed the Pageable version; now return all badges for the user profile.
    List<UserBadge> findByUserProfile(UserProfile userProfile);

    boolean existsByUserProfileAndBadgeName(UserProfile userProfile, String badgeName);
}