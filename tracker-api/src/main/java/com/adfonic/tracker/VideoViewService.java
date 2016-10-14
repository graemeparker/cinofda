package com.adfonic.tracker;

import java.util.List;

import com.adfonic.adserver.Click;

/**
 * Service for tracking video views
 */
public interface VideoViewService {
    /**
     * Track that an end user viewed a video clip as a result of click-to-video.
     * This method will de-dup the view, in that we only track the first view
     * associated with a given click.
     * @param click the Click with which the view is associated
     * @param viewMs the duration of the video clip, in milliseconds, that the
     * user actually viewed
     * @param clipMs the total length of the video in milliseconds
     * @return true if this was the first view for the given click, or false if
     * this is a duplicate view
     */
    boolean trackVideoView(Click click, int viewMs, int clipMs);

    /**
     * Schedule a retry of a video view tracking request.  This should be called
     * when the tracker fails to find the respective click.
     */
    void scheduleVideoViewRetry(String clickExternalId, int viewMs, int clipMs);

    /**
     * Schedule a retry of a video view tracking request.  This should be called
     * when the tracker fails to find the respective click.
     */
    void scheduleRetry(PendingVideoView pendingVideoView);

    /**
     * Deletes a scheduled retry of a video view tracking request from the db.
     * This should be called once the retry was successfully processed.
     */
    void deleteScheduledVideoViewRetry(PendingVideoView pendingVideoView);

    /**
     * Get a list of pending video views to retry.  This is intended to be used by
     * the app that will periodically reattempt to track video views that have been
     * scheduled for retry.
     */
    List<PendingVideoView> getPendingVideoViewsToRetry(int maxRows);
}
