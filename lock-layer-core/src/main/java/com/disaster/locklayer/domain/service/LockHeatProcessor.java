package com.disaster.locklayer.domain.service;

import com.disaster.locklayer.domain.share.LockHeartBeatEntity;

/**
 * The interface Lock heat processor.
 *
 * @author disaster
 * @version 1.0
 */
public interface LockHeatProcessor {
    /**
     * Lock heart removed processor.
     */
    void lockHeartRemovedProcessor(LockHeartBeatEntity value);
}
