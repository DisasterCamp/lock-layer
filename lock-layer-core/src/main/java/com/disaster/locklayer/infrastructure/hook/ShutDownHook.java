package com.disaster.locklayer.infrastructure.hook;

import com.disaster.locklayer.domain.share.LockManager;
import com.disaster.locklayer.infrastructure.utils.LoggerUtil;

import java.util.concurrent.ExecutorService;

public class ShutDownHook extends Thread {
    private LockManager lockManager;

    public ShutDownHook(LockManager lockManager) {
        this.lockManager = lockManager;
    }

    @Override
    public void run() {
        ExecutorService retryLockExecutorService = lockManager.getRetryLockExecutorService();
        ExecutorService executorService = lockManager.getExecutorService();
        if (!retryLockExecutorService.isShutdown()||!executorService.isShutdown()){
            LoggerUtil.println(this.getClass(), "lock layer shutdown hook");
            retryLockExecutorService.shutdownNow();
            executorService.shutdownNow();
        }
    }
}
