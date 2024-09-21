package com.disaster.locklayer.locklayer.domain.service;

import com.disaster.locklayer.domain.share.LockHeartBeatEntity;

/**
 * The interface Lock heat processor.
 */
public interface LockHeatProcessor {
    /**
     * Lock heart removed processor.
     */
    void lockHeartRemovedProcessor(LockHeartBeatEntity value);
}
