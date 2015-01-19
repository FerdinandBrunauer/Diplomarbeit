package datapoint;

import database.DatabaseConnection;

/**
 * Copyright 2015 (C) HTL - Hallein
 * Created on:  13.01.2015
 * Author:      Ferdinand
 */
public abstract class SuperDatapoint implements Runnable {
    protected Validator validator;
    protected DatabaseConnection databaseConnection;

    public SuperDatapoint(Validator validator) {
        this.validator = validator;
//        this.databaseConnection = DatabaseConnection.getInstance();
    }

    protected abstract void fireEvent(Object... objects);
}
