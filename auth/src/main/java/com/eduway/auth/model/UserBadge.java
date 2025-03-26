package com.eduway.auth.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profile_badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserBadge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_profile_id", nullable = false)
    @JsonBackReference
    private UserProfile userProfile;

    private String badgeName;


    // Fixed constructor
    public UserBadge(UserProfile userProfile, String badgeName) {
        this.userProfile = userProfile;
        this.badgeName = badgeName;
    }
}
