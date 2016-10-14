package com.adfonic.tasks.combined;

import com.adfonic.tasks.SpringTaskBase;

public class TestFxRateTasks {

    static class FxRateTasksRunner extends FxRateTasks implements Runnable {

        @Override
        public void run() {
            super.doFetchRate();
        }

        public static void main(String[] args) {
            int exitCode = 0;
            try {
                SpringTaskBase.runBean(FxRateTasks.class, "adfonic-toolsdb-context.xml", "adfonic-tasks-context.xml", "adfonic-optdb-context.xml");
            } catch (Exception e) {
                System.err.println("Exception caught " + e);
                exitCode = 1;
            } finally {
                Runtime.getRuntime().exit(exitCode);
            }
        }
    }

}
