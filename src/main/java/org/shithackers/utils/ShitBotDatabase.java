package org.shithackers.utils;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ShitBotDatabase {
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "root";
    private static final String URL = "jdbc:postgresql://localhost:5432/ShitBot_db";

    private static volatile DSLContext dslContext;

    public static DSLContext getDSLContext() {
        if (dslContext == null) {
            synchronized (ShitBotDatabase.class) {
                if (dslContext == null) {
                    try {
                        Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                        dslContext = DSL.using(connection, SQLDialect.POSTGRES);
                    } catch (SQLException e) {
                        throw new RuntimeException("Failed to initialize the database connection", e);
                    }
                }
            }
        }
        return dslContext;
    }
}
