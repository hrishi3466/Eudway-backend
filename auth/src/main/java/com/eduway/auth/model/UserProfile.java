package com.eduway.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String fullName;
    private String email;
    private String phone;
    private String bio;
    private String skills;
    private String experience;
    private String education;
    private String linkedin;
    private String github;

    @ElementCollection
    private List<String> badges = new ArrayList<>();  // Awarded badges

    @ElementCollection
    @CollectionTable(name = "learning_paths", joinColumns = @JoinColumn(name = "profile_id"))
    @MapKeyColumn(name = "path_id")
    @Column(name = "path_topics")
    private Map<String, List<String>> learningPaths = new HashMap<>(); // Map of learning path ID to topics

    @ElementCollection
    @CollectionTable(name = "completed_topics", joinColumns = @JoinColumn(name = "profile_id"))
    @MapKeyColumn(name = "path_id")
    @Column(name = "completed_topics")
    private Map<String, List<String>> completedTopicsByPath = new HashMap<>(); // Map of learning path ID to completed topics

    // Getters and setters...
    @Setter
    @Getter
    @ElementCollection
    @CollectionTable(name = "completed_paths", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "completed_path_id")
    private List<String> completedLearningPaths = new ArrayList<>(); // List of completed learning path IDs


}