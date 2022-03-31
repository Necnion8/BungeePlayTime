package com.gmail.necnionch.myplugin.bungeeplaytime.bungee.errors;

import java.sql.SQLException;

public class DatabaseError extends Error {
    public DatabaseError(SQLException exception) {
        super(exception);
    }

}
