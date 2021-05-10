package com.sirdave.database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Configuration {

    private static final String url = "jdbc:postgresql://localhost/supermarket";
    private static final String user = System.getenv("DB_USER");
    private static final String password = System.getenv("DB_PASSWORD");

    public Connection getConnection(){
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;

    }

}
