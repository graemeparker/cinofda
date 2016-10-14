package com.adfonic.domain.cache.ext.util;

import java.text.NumberFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * Implementing our own as Spring's can't run multiple task at the same time
 */
public class AdfonicStopWatch {

    ConcurrentHashMap<String, TaskDetail> taskMap = new ConcurrentHashMap<String, TaskDetail>();
    ConcurrentLinkedQueue<TaskDetail> allTasks = new ConcurrentLinkedQueue<AdfonicStopWatch.TaskDetail>();

    public void start(String taskName) {
        TaskDetail taskDetail = new TaskDetail();
        TaskDetail taskDetailInMap = taskMap.putIfAbsent(taskName, taskDetail);
        if (taskDetailInMap == null) {
            taskDetail.startTime = System.nanoTime();
            taskDetail.taskName = taskName;
            allTasks.add(taskDetail);
        } else {
            throw new RuntimeException("Task " + taskName + " already started");
        }

    }

    public void stop(String taskName) {
        Long endTime = System.nanoTime();
        TaskDetail taskDetailInMap = taskMap.get(taskName);
        if (taskDetailInMap == null) {
            throw new RuntimeException("No task with name " + taskName + " running");
        }
        if (taskDetailInMap.endTime != null && taskDetailInMap.endTime > 0) {
            throw new RuntimeException("Task with name " + taskName + " has already been stopped at " + taskDetailInMap.endTime);
        }
        taskDetailInMap.endTime = endTime;
    }

    public String prettyPrint() {
        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        if (allTasks.isEmpty()) {
            sb.append("No task info kept");
        } else {
            sb.append("-----------------------------------------\n");
            sb.append("ms        %     Task name\n");
            sb.append("-----------------------------------------\n");
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMinimumIntegerDigits(8);
            nf.setGroupingUsed(false);
            NumberFormat pf = NumberFormat.getPercentInstance();
            pf.setMinimumIntegerDigits(3);
            pf.setGroupingUsed(false);
            long totalTime = 0;
            Long taskEndTime;
            for (TaskDetail task : allTasks) {
                taskEndTime = task.endTime;
                if (task.endTime == null || task.endTime == 0) {
                    taskEndTime = System.nanoTime();
                }
                totalTime = totalTime + (taskEndTime - task.startTime) / 1000000;
            }
            long taskTime;
            boolean stillRunning;
            for (TaskDetail task : allTasks) {
                taskEndTime = task.endTime;
                stillRunning = false;
                if (task.endTime == null || task.endTime == 0) {
                    taskEndTime = System.nanoTime();
                    stillRunning = true;
                }
                taskTime = (taskEndTime - task.startTime) / 1000000;
                sb.append(nf.format(taskTime)).append("  ");
                sb.append(pf.format((double) taskTime / totalTime)).append("  ");
                sb.append(task.taskName);
                if (stillRunning) {
                    sb.append("(Still Running or forgot to Stop)");
                }
                sb.append("\n");
            }
            sb.append("-----------------------------------------\n");
            sb.append(nf.format(totalTime)).append("  ");
            sb.append("Total Time(ms)\n");
        }
        return sb.toString();
    }

    private class TaskDetail {
        public Long startTime;
        public Long endTime;
        public String taskName;
    }
}
