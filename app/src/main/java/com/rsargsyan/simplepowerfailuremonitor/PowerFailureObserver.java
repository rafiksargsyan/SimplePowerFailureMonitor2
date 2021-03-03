package com.rsargsyan.simplepowerfailuremonitor;

public interface PowerFailureObserver {
    void observe(boolean powerIsOn);
    void destroy();
}
