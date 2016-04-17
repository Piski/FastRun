package com.example.sergei.fastrun;

import android.os.SystemClock;

public class RunTimer {
    private long startTime;
    private long endTime;

    public void start() {
        startTime = SystemClock.elapsedRealtime();
    }

    public void stop() {
        endTime = SystemClock.elapsedRealtime();
    }

    public double getTime() {
        return (endTime - startTime) / 1000.0;
    }

}
