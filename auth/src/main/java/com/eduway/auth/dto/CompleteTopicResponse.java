package com.eduway.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class CompleteTopicResponse {
    private String message;
    private List<String> completedTopics;
    private List<String> badges;
}
