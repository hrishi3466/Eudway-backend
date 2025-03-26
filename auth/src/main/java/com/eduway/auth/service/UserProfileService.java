package com.eduway.auth.service;

import com.eduway.auth.dto.BadgeDTO;
import com.eduway.auth.model.User;
import com.eduway.auth.model.UserBadge;
import com.eduway.auth.model.UserProfile;
import com.eduway.auth.repository.UserBadgeRepository;
import com.eduway.auth.repository.UserProfileRepository;
import com.eduway.auth.repository.UserRepository;
import org.hibernate.query.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.awt.print.Pageable;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository; // Step 3: Inject UserBadgeRepository

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

        // Preserve existing learning paths
        profile.setLearningPaths(Optional.ofNullable(updatedProfile.getLearningPaths()).orElse(new HashMap<>()));
        profile.setCompletedTopicsByPath(Optional.ofNullable(updatedProfile.getCompletedTopicsByPath()).orElse(new HashMap<>()));

        // Save UserProfile first
        profile = userProfileRepository.save(profile);
        return profile;
    }

    public void deleteUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userProfileRepository.deleteByUser(user);
    }

    public UserProfile saveLearningPath(String username, List<String> learningPathTopics) {
        UserProfile profile = getUserProfile(username);

        String pathId = UUID.randomUUID().toString();
        profile.getLearningPaths().put(pathId, new ArrayList<>(learningPathTopics));
        profile.getCompletedTopicsByPath().put(pathId, new ArrayList<>());

        return userProfileRepository.save(profile);
    }

    public Map<String, Object> markTopicAsCompleted(String username, String topic, String learningPathId) {
        UserProfile profile = getUserProfile(username);
        Map<String, Object> response = new HashMap<>();

        if (profile.getId() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User profile ID is missing.");
        }

        if (profile.getLearningPaths() == null || !profile.getLearningPaths().containsKey(learningPathId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Learning path not found for user: " + username);
        }

        List<String> pathTopics = profile.getLearningPaths().get(learningPathId);
        if (pathTopics == null || !pathTopics.contains(topic)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Topic '" + topic + "' not found in learning path.");
        }

        profile.getCompletedTopicsByPath().computeIfAbsent(learningPathId, k -> new ArrayList<>());

        List<String> completedTopics = profile.getCompletedTopicsByPath().get(learningPathId);
        if (!completedTopics.contains(topic)) {
            completedTopics.add(topic);
        }

        boolean pathCompleted = new HashSet<>(completedTopics).containsAll(pathTopics);
        if (pathCompleted && !profile.getCompletedLearningPaths().contains(learningPathId)) {
            profile.getCompletedLearningPaths().add(learningPathId);

            String badgeName = "Learning Path Master: " + getLearningPathName(learningPathId, profile);

            // 🚀 **Prevent duplicate badge creation**
            if (!userBadgeRepository.existsByUserProfileAndBadgeName(profile, badgeName)) {
                UserBadge newBadge = new UserBadge(profile, badgeName);
                userBadgeRepository.save(newBadge);
                response.put("newBadge", badgeName);
            }
        }


        // Save UserProfile
        try {
            profile = userProfileRepository.save(profile);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save user profile", e);
        }

        double progress = pathTopics.isEmpty() ? 0.0 : ((double) completedTopics.size() / pathTopics.size()) * 100;
        response.put("success", true);
        response.put("topicCompleted", topic);
        response.put("progress", progress);
        response.put("pathCompleted", pathCompleted);
        return response;
    }


    public List<BadgeDTO> getUserBadges(String username) {
        UserProfile profile = getUserProfile(username);

        return userBadgeRepository.findByUserProfile(profile)
                .stream()
                .map(badge -> {
                    BadgeDTO dto = new BadgeDTO();
                    dto.setId(badge.getId());
                    dto.setBadgeName(badge.getBadgeName());
                    return dto;
                })
                .collect(Collectors.toList());
    }


    public Map<String, Object> getLearningProgress(String username) {
        UserProfile profile = getUserProfile(username);
        Map<String, Object> progress = new HashMap<>();

        profile.getLearningPaths().forEach((pathId, pathTopics) -> {
            List<String> completedTopics = profile.getCompletedTopicsByPath()
                    .getOrDefault(pathId, Collections.emptyList());

            double progressPercentage = pathTopics.isEmpty() ? 0.0 :
                    ((double) completedTopics.size() / pathTopics.size()) * 100;

            progress.put(pathId, Map.of(
                    "totalTopics", pathTopics.size(),
                    "completedTopics", completedTopics.size(),
                    "progressPercentage", progressPercentage,
                    "isCompleted", profile.getCompletedLearningPaths().contains(pathId)
            ));
        });

        return progress;
    }


    private String getLearningPathName(String learningPathId, UserProfile profile) {
        return profile.getLearningPaths().getOrDefault(learningPathId, new ArrayList<>()).isEmpty()
                ? "Unknown Path" : "Path " + learningPathId;
    }
}
