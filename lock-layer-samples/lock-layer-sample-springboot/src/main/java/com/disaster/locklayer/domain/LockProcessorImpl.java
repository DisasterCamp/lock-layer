package com.disaster.locklayer.domain;

import com.disaster.locklayer.domain.service.LockProcessor;
import com.disaster.locklayer.domain.share.LockEntity;
import org.springframework.stereotype.Service;

@Service
public class LockProcessorImpl implements LockProcessor {
    @Override
    public void failLockProcessor(LockEntity lockEntity) {
        System.out.println("lock failure"+lockEntity.get_key());
    }

    @Override
    public void successLockProcessor(LockEntity lockEntity) {
        System.out.println("lock success"+lockEntity.get_key());
    }
}
