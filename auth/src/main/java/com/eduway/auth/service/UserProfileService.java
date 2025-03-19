package com.eduway.auth.service;

import com.eduway.auth.model.User;
import com.eduway.auth.model.UserProfile;
import com.eduway.auth.repository.UserProfileRepository;
import com.eduway.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    public UserProfile getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return userProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    public UserProfile createOrUpdateProfile(String username, UserProfile updatedProfile) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserProfile profile = userProfileRepository.findByUser(user).orElse(new UserProfile());

        profile.setUser(user);
        profile.setFullName(updatedProfile.getFullName());
        profile.setEmail(updatedProfile.getEmail());
        profile.setPhone(updatedProfile.getPhone());
        profile.setBio(updatedProfile.getBio());
        profile.setSkills(updatedProfile.getSkills());
        profile.setExperience(updatedProfile.getExperience());
        profile.setEducation(updatedProfile.getEducation());
        profile.setLinkedin(updatedProfile.getLinkedin());
        profile.setGithub(updatedProfile.getGithub());

        // Preserve existing badges and learning paths
        if (updatedProfile.getBadges() != null && !updatedProfile.getBadges().isEmpty()) {
            profile.setBadges(updatedProfile.getBadges());
        }

        if (updatedProfile.getLearningPaths() != null && !updatedProfile.getLearningPaths().isEmpty()) {
            profile.setLearningPaths(updatedProfile.getLearningPaths());
        }

        if (updatedProfile.getCompletedTopicsByPath() != null && !updatedProfile.getCompletedTopicsByPath().isEmpty()) {
            profile.setCompletedTopicsByPath(updatedProfile.getCompletedTopicsByPath());
        }

        return userProfileRepository.save(profile);
    }

    public void deleteUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userProfileRepository.deleteByUser(user);
    }

    public UserProfile saveLearningPath(String username, List<String> learningPathTopics) {
        UserProfile profile = getUserProfile(username);

        // Generate a unique ID for this learning path
        String pathId = UUID.randomUUID().toString();

        // Store the learning path
        profile.getLearningPaths().put(pathId, learningPathTopics);

        // Initialize an empty list for completed topics in this path
        profile.getCompletedTopicsByPath().put(pathId, new ArrayList<>());

        return userProfileRepository.save(profile);
    }

    public Map<String, Object> markTopicAsCompleted(String username, String topic, String learningPathId) {
        UserProfile profile = getUserProfile(username);
        Map<String, Object> response = new HashMap<>();

        // Validate learning path exists
        if (!profile.getLearningPaths().containsKey(learningPathId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Learning path not found");
        }

        List<String> pathTopics = profile.getLearningPaths().get(learningPathId);

        // Validate topic is in the learning path
        if (!pathTopics.contains(topic)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Topic not in this learning path");
        }

        // Get or initialize completed topics for this path
        List<String> completedTopics = profile.getCompletedTopicsByPath().getOrDefault(learningPathId, new ArrayList<>());

        // Add the topic to completed if not already completed
        if (!completedTopics.contains(topic)) {
            completedTopics.add(topic);
            profile.getCompletedTopicsByPath().put(learningPathId, completedTopics);
        }

        // Check if all topics in the path are completed
        boolean pathCompleted = completedTopics.containsAll(pathTopics);

        // If path is completed, award badge if not already awarded
        if (pathCompleted && !profile.getCompletedLearningPaths().contains(learningPathId)) {
            // Add the path to completed paths
            profile.getCompletedLearningPaths().add(learningPathId);

            // Award badge
            String badgeName = "Learning Path Master: " + learningPathId; // This could be improved with a proper path name
            if (!profile.getBadges().contains(badgeName)) {
                profile.getBadges().add(badgeName);
                response.put("newBadge", badgeName);
            }
        }

        userProfileRepository.save(profile);

        // Prepare response
        response.put("success", true);
        response.put("topicCompleted", topic);
        response.put("progress", (double) completedTopics.size() / pathTopics.size() * 100);
        response.put("pathCompleted", pathCompleted);

        return response;
    }

    public List<String> getUserBadges(String username) {
        UserProfile profile = getUserProfile(username);
        return profile.getBadges();
    }

    public Map<String, Object> getLearningProgress(String username) {
        UserProfile profile = getUserProfile(username);
        Map<String, Object> progress = new HashMap<>();

        // For each learning path, calculate progress
        for (Map.Entry<String, List<String>> entry : profile.getLearningPaths().entrySet()) {
            String pathId = entry.getKey();
            List<String> pathTopics = entry.getValue();
            List<String> completedTopics = profile.getCompletedTopicsByPath().getOrDefault(pathId, new ArrayList<>());

            Map<String, Object> pathProgress = new HashMap<>();
            pathProgress.put("totalTopics", pathTopics.size());
            pathProgress.put("completedTopics", completedTopics.size());
            pathProgress.put("progressPercentage", (double) completedTopics.size() / pathTopics.size() * 100);
            pathProgress.put("isCompleted", profile.getCompletedLearningPaths().contains(pathId));

            progress.put(pathId, pathProgress);
        }

        return progress;
    }
}