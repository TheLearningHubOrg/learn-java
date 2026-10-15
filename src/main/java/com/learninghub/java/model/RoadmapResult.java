package com.learninghub.java.model;

import java.util.List;

/**
 * Sealed interface demonstrating Java 21 algebraic data types and pattern matching.
 */
public sealed interface RoadmapResult permits RoadmapResult.Success, RoadmapResult.Failure {

    record Success(
        int totalStages,
        int completedStages,
        double completionPercentage,
        List<RoadmapStage> stages
    ) implements RoadmapResult {
        public Success {
            stages = List.copyOf(stages);
        }
    }

    record Failure(String reason, Throwable cause) implements RoadmapResult {
        public Failure(String reason) {
            this(reason, null);
        }
    }
}
