package at.htlhallein.scroll;

import java.util.EventObject;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class ScrollEventObject extends EventObject {
    private ScrollEventDirection direction;

    public ScrollEventObject(Object source, ScrollEventDirection direction) {
        super(source);
        this.direction = direction;
    }
}


