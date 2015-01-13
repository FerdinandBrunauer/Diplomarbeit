package at.htlhallein.scroll;

import java.util.EventListener;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public interface ScrollEventListener extends EventListener {
    public void fireScrollEvent(ScrollEventObject eventObject);
}
