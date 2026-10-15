package com.learninghub.java.model;

import java.util.List;

/**
 * Immutable domain record representing a milestone in the Modern Java 21+ curriculum.
 */
public record RoadmapStage(
    int stageNumber,
    String title,
    String domainFocus,
    boolean completed,
    List<String> keyTopics
) {
    public RoadmapStage {
        if (stageNumber < 1 || stageNumber > 7) {
            throw new IllegalArgumentException("Stage number must be between 1 and 7, got: " + stageNumber);
        }
        keyTopics = List.copyOf(keyTopics);
    }
}
