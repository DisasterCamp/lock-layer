package com.disaster.locklayer.locklayer.domain.service;

import com.disaster.locklayer.domain.share.LockEntity;

/**
 * The interface Lock processor.
 *
 * @author disaster
 * @version 1.0
 */
public interface LockProcessor {
    /**
     * Fail lock processor.
     */
    void failLockProcessor(LockEntity lockEntity);

    /**
     * Success lock processor.
     */
    void successLockProcessor(LockEntity lockEntity);


}
