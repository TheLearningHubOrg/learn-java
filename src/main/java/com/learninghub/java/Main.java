package com.learninghub.java;

import com.learninghub.java.model.RoadmapResult;
import com.learninghub.java.model.RoadmapStage;
import com.learninghub.java.service.VirtualThreadRoadmapExecutor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class Main {

    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println("  The Learning Hub - Modern Java 21+ & Virtual Threads");
        System.out.println("======================================================");
        System.out.println("Runtime: " + System.getProperty("java.runtime.name") + " " + System.getProperty("java.version"));
        System.out.println("VM: " + System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + ")");
        System.out.println("Available Processors: " + Runtime.getRuntime().availableProcessors());

        var executor = new VirtualThreadRoadmapExecutor();
        int simulatedConcurrentTasks = 500;
        System.out.printf("Spawning %,d Project Loom Virtual Threads concurrently...\n\n", simulatedConcurrentTasks);

        long startNs = System.nanoTime();
        RoadmapResult result = executor.evaluateRoadmapWithVirtualThreads(simulatedConcurrentTasks);
        long durationMs = (System.nanoTime() - startNs) / 1_000_000;

        // Java 21 Pattern Matching for switch on sealed interface
        switch (result) {
            case RoadmapResult.Success(int total, int completed, double percentage, var stages) -> {
                System.out.printf("Completed Virtual Thread Evaluation in %d ms.\n\n", durationMs);
                System.out.println("7-Stage Pedagogical Roadmap:");
                System.out.println("------------------------------------------------------");
                for (RoadmapStage stage : stages) {
                    String badge = stage.completed() ? "[OK]" : "[..]";
                    System.out.printf(" %s Stage %d: %s\n", badge, stage.stageNumber(), stage.title());
                    System.out.printf("      Focus: %s\n", stage.domainFocus());
                    System.out.printf("      Topics: %s\n", String.join(", ", stage.keyTopics()));
                }
                System.out.println("------------------------------------------------------");
                System.out.printf("Progress: %d/%d Stages (%.1f%%)\n", completed, total, percentage);
            }
            case RoadmapResult.Failure(String reason, Throwable cause) -> {
                System.err.println("Audit Failed: " + reason);
                if (cause != null) {
                    cause.printStackTrace();
                }
                System.exit(1);
            }
        }

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long heapUsedMb = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long heapMaxMb = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        System.out.printf("JVM Heap Usage: %d MB / %d MB\n", heapUsedMb, heapMaxMb);
        System.out.println("======================================================\n");
    }
}
