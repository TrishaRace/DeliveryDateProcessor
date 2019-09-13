package com.repsol.repsolcamera.utility.ui;


import com.repsol.repsolcamera.RepsolCamera;

public interface FastScrollStateChangeListener {

    /**
     * Called when fast scrolling begins
     */
    void onFastScrollStart(RepsolCamera fastScroller);

    /**
     * Called when fast scrolling ends
     */
    void onFastScrollStop(RepsolCamera fastScroller);
}
