package at.htlhallein.datapoint;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public abstract class SuperDatapoint implements Runnable {
    // TODO Databaseobjekt threadsafe

    protected Validator validator;

    public SuperDatapoint(Validator validator) {
        this.validator = validator;
    }

    protected abstract void fireEvent(Object... objects);
}
