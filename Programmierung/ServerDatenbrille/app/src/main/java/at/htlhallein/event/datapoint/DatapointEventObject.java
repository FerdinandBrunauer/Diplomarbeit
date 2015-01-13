package at.htlhallein.event.datapoint;

import java.util.EventObject;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public class DatapointEventObject extends EventObject {
    private String htmlText;

    public DatapointEventObject(Object source, String htmlText) {
        super(source);
        this.htmlText = htmlText;
    }

    public String getHtmlText() {
        return this.htmlText;
    }
}
