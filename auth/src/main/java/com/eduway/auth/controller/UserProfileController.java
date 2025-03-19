package com.eduway.auth.controller;

import com.eduway.auth.dto.CompleteTopicRequest;
import com.eduway.auth.model.UserProfile;
import com.eduway.auth.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:5173")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @GetMapping("/{username}")
    public ResponseEntity<UserProfile> getProfile(@PathVariable String username) {
        return ResponseEntity.ok(userProfileService.getUserProfile(username));
    }

    @PutMapping("/{username}")
    public ResponseEntity<UserProfile> updateProfile(@PathVariable String username,
                                                     @RequestBody UserProfile updatedProfile) {
        return ResponseEntity.ok(userProfileService.createOrUpdateProfile(username, updatedProfile));
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<String> deleteProfile(@PathVariable String username) {
        userProfileService.deleteUserProfile(username);
        return ResponseEntity.ok("Profile deleted successfully");
    }

    @PostMapping("/{username}/save-learning-path")
    public ResponseEntity<UserProfile> saveLearningPath(@PathVariable String username,
                                                        @RequestBody List<String> learningPath) {
        return ResponseEntity.ok(userProfileService.saveLearningPath(username, learningPath));
    }

    @PostMapping("/{username}/complete-topic")
    public ResponseEntity<Map<String, Object>> completeTopic(@PathVariable String username,
                                                             @RequestBody CompleteTopicRequest request) {
        return ResponseEntity.ok(userProfileService.markTopicAsCompleted(username, request.getTopic(), request.getLearningPathId()));
    }

    @GetMapping("/{username}/badges")
    public ResponseEntity<List<String>> getUserBadges(@PathVariable String username) {
        return ResponseEntity.ok(userProfileService.getUserBadges(username));
    }

    @GetMapping("/{username}/learning-progress")
    public ResponseEntity<Map<String, Object>> getLearningProgress(@PathVariable String username) {
        return ResponseEntity.ok(userProfileService.getLearningProgress(username));
    }
}