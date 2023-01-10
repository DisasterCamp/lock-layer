package com.disaster.locklayer.domain;

import com.disaster.locklayer.domain.service.LockHeatProcessor;
import com.disaster.locklayer.domain.share.LockHeartBeatEntity;
import org.springframework.stereotype.Service;

@Service
public class LockHeatProcessorImpl implements LockHeatProcessor {
    @Override
    public void lockHeartRemovedProcessor(LockHeartBeatEntity value) {
        System.out.println(value.getExpireCount());
    }
}
