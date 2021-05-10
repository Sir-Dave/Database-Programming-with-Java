package com.sirdave.database;

import com.sirdave.database.Models.Customer;

import java.sql.*;
import java.util.Scanner;

public class Main {

    private Connection getConnection(){
        Configuration configuration = new Configuration();
        return configuration.getConnection();
    }

    private void addNewCustomer(String name) throws SQLException {
        String query = "insert into customers(customer_name) values(?)";
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setString(1, name);

        statement.execute();
    }

    private void getData(int number) throws SQLException {
        Connection connection = getConnection();
        String SQL = "select * from customers where customer_id <=?";
        PreparedStatement statement = connection.prepareStatement(SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setInt(1, number);
        ResultSet resultSet = statement.executeQuery();//"select * from customers");

        while (resultSet.next()){
            int id = resultSet.getInt("customer_id");
            String name = resultSet.getString("customer_name");
            Customer customer = new Customer(id, name);
            System.out.println(customer.getName());
        }

        System.out.println();
    }

    public static void main(String[] args) throws SQLException {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter customer name: ");
        String name = input.nextLine();

        Main main = new Main();
        main.addNewCustomer(name);


    }
}
