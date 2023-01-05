package com.disaster.locklayer.domain;

import com.disaster.locklayer.infrastructure.annotations.LockLayer;
import org.springframework.stereotype.Component;

@Component
public class Lock {

    @LockLayer(key = "test_key")
    public void lock(){

    }

    @LockLayer(key = "test_key")
    public void lockException(){
        throw new RuntimeException();
    }
}
