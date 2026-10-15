package com.learninghub.java;

import com.learninghub.java.model.RoadmapResult;
import com.learninghub.java.model.RoadmapStage;
import com.learninghub.java.service.VirtualThreadRoadmapExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoadmapServiceTest {

    private VirtualThreadRoadmapExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new VirtualThreadRoadmapExecutor();
    }

    @Test
    @DisplayName("Curriculum contains exactly 7 stages with sequential IDs")
    void testCurriculumStages() {
        List<RoadmapStage> curriculum = executor.getCurriculum();
        assertNotNull(curriculum);
        assertEquals(7, curriculum.size());

        for (int i = 0; i < curriculum.size(); i++) {
            assertEquals(i + 1, curriculum.get(i).stageNumber());
            assertFalse(curriculum.get(i).title().isBlank());
            assertFalse(curriculum.get(i).keyTopics().isEmpty());
        }
    }

    @Test
    @DisplayName("Stage record enforces immutable topic copies")
    void testStageRecordImmutability() {
        RoadmapStage stage = executor.getCurriculum().getFirst();
        assertThrows(UnsupportedOperationException.class, () -> stage.keyTopics().add("New Topic"));
    }

    @Test
    @DisplayName("Virtual threads executor completes concurrent tasks and returns Success")
    void testVirtualThreadsExecution() {
        RoadmapResult result = executor.evaluateRoadmapWithVirtualThreads(50);
        assertNotNull(result);

        // Pattern matching assertion
        if (result instanceof RoadmapResult.Success success) {
            assertEquals(7, success.totalStages());
            assertTrue(success.completedStages() >= 5);
            assertTrue(success.completionPercentage() > 0 && success.completionPercentage() <= 100);
            assertEquals(7, success.stages().size());
        } else {
            fail("Expected RoadmapResult.Success, received Failure");
        }
    }
}
