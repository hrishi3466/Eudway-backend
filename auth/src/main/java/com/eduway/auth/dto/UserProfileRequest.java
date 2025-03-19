package com.eduway.auth.dto;

import java.util.List;

public class UserProfileRequest {
    private String fullName;
    private String email;
    private String phone;
    private String bio;
    private String skills;
    private String experience;
    private String education;
    private String linkedin;
    private String github;
    private List<String> badges;
    private List<String> savedCourses;
    private List<String> savedLearningPaths; // New field

    // Getters and Setters
    public List<String> getSavedLearningPaths() {
        return savedLearningPaths;
    }

    public void setSavedLearningPaths(List<String> savedLearningPaths) {
        this.savedLearningPaths = savedLearningPaths;
    }
}
