package com.sirdave.database;

import com.sirdave.database.Models.Inventory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static final Connection connection = Configuration.getConnection();

    // This function checks for items whose quantities need to be replenished
    private ArrayList<Inventory> checkItemsNeedReplenishment() throws SQLException {
        ArrayList<Inventory> inventories = new ArrayList<>();
        String query = "select * from inventory where quantity<5";
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet resultSet = statement.executeQuery(query);

        while (resultSet.next()){
            int id = resultSet.getInt("product_id");
            String name = resultSet.getString("product_name");
            int quantity = resultSet.getInt("quantity");

            Inventory inventory = new Inventory(id, name, quantity);
            inventories.add(inventory);
        }
        return inventories;
    }

    // helper function to check for the current quantity of a selected item
    private int checkNumItem(String item) throws SQLException {
        String query = "select * from inventory where product_name=?";
        PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setString(1, item);
        ResultSet resultSet = statement.executeQuery();
        Inventory inventory = null;

        while (resultSet.next()){
            int id = resultSet.getInt("product_id");
            String name = resultSet.getString("product_name");
            int quantity = resultSet.getInt("quantity");

            inventory = new Inventory(id, name, quantity);
            System.out.println(inventory.getQuantity());
        }
        assert inventory != null;
        return inventory.getQuantity();
    }

    // helper function to automatically update the quantity of a selected item
    private void updateItem(String item, int quantity) throws SQLException {
        String query = "update inventory set quantity=? where name=?";
        PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setInt(1, quantity);
        statement.setString(2, item);
        statement.execute();
    }

    // function to add 20 to items whose quantities are less than 5
    private void replenishStock(String item, int quantity) throws SQLException {
        String query = "update inventory set quantity=? where product_name=?";
        PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setInt(1, quantity + 20);
        statement.setString(2, item);
        statement.execute();
    }

    private void addNewCustomer(){

    }

    private void makeOrder(){

    }

    public static void main(String[] args) throws SQLException {
        Main main = new Main();
        Scanner input = new Scanner(System.in);
        System.out.println("Welcome to ABC Supermarket, how may we serve you today?: ");
        System.out.println("1. Check items that need replenishment\n2. Check number of items left\n3.Replenish stock");
        System.out.println("Enter a number to continue: ");
        int choice = input.nextInt();
        switch (choice){
            case 1:
                main.checkItemsNeedReplenishment();
                break;

            case 2: {
                System.out.println( "Enter the name of the item you want to check for: ");
                String item = input.next();
                main.checkNumItem(item);
                break;
            }
            case 3:{
                System.out.println( "Enter the name of the item you want to update: ");
                String item = input.next();
                int currentQuantity = main.checkNumItem(item);
                main.replenishStock(item, currentQuantity);
                break;
            }

            default:
                System.out.println("Invalid choice!");
        }
    }
}
