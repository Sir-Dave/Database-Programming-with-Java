package com.sirdave.database;

import com.sirdave.database.Models.Customer;
import com.sirdave.database.Models.Inventory;
import com.sirdave.database.Models.Order;
import com.sirdave.database.Models.Product;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

        resultSet.first(); // because we want to get the most recent inventory

        int id = resultSet.getInt("product_id");
        String name = resultSet.getString("product_name");
        int quantity = resultSet.getInt("quantity");

        Inventory inventory = new Inventory(id, name, quantity);
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

    // returns true if the customer already exists in the database
    private boolean isCustomerExists(String name) throws SQLException{
        String query = "select * from customers where customer_name=?";
        PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.first();
    }

    private void addNewCustomer(String name) throws SQLException{
        if (!isCustomerExists(name)){
            String query = "insert into customers(customer_name) values(?)";
            PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            statement.setString(1, name);
            statement.execute();
        }
    }

    private int findCustomerById(String name) throws SQLException {
        String query = "select * from customers where customer_name=?";
        PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();
        resultSet.first(); // because there can only be one user with that name
        int customerId = resultSet.getInt("customer_id");
        String customerName = resultSet.getString("customer_name");
        Customer customer = new Customer(customerId, customerName);
        return customer.getId();
    }

    // checks if the product exists in the database
    private boolean isProductExists(String name) throws SQLException{
        String query = "select * from products where lower(name)=?";
        PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setString(1, name.toLowerCase());
        ResultSet resultSet = statement.executeQuery();
        return resultSet.first();
    }

    // do a case-insensitive search for the product details
    private Product getProductDetails(String name) throws SQLException {
        String query = "select * from products where lower(name)=?";
        PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setString(1, name.toLowerCase());
        ResultSet resultSet = statement.executeQuery();
        resultSet.first(); // because there can only be one product with that name
        int id = resultSet.getInt("id");
        String productName = resultSet.getString("name");
        int price = resultSet.getInt("price");
        return new Product(id, productName, price);
    }


    private void initiateOrder() throws SQLException {
        ArrayList<Order> orderList = new ArrayList<>();

        Scanner input = new Scanner(System.in);
        System.out.println("Please enter your name: ");
        String userName = input.nextLine();
        int userId, quantity;
        try{

            userId = findCustomerById(userName);
        }
        catch (SQLException e){
            addNewCustomer(userName);
            userId = findCustomerById(userName);
        }

        char choice;
        do{
            System.out.println("Please enter the item you want to buy: ");
            String itemName = input.next();

            if (isProductExists(itemName)){
                System.out.println("How many " + itemName+ "(s) do you want to order?: ");
                quantity = input.nextInt();
                Order order = new Order(itemName, quantity);
                orderList.clear();
                orderList.add(order);
                makeOrder(userId, orderList);
            }
            else {
                System.out.println("Sorry, item not found!");
            }

            System.out.println("Do you want to add more item (y/n)? ");
            choice = input.next().charAt(0);
        }

        while(choice != 'n');
    }

    private void makeOrder(int customerId, List<Order> orders) throws SQLException {
        double discount = 0;
        for (Order order: orders){
            String name = order.getName();
            int quantity = order.getQuantity();
            Product product = getProductDetails(name);
            double price = product.getPrice();
            double amount = quantity * price;
            if (quantity >=5){                  //5% discount on any product that has at least 5 of it purchased
                discount = 0.05 * amount;
            }
            amount = amount - discount;
            saveOrder(customerId, product.getId(), quantity, discount, amount);

            // TODO: should also call the updateItem() function to subtract
            //  the remaining quantities of each item bought
        }
    }

    // save the order to database
    private void saveOrder(int customerId, int productId, int quantity, double discount,
                           double amount) throws SQLException{
        String query = "insert into sales(customer_id, product_id, quantity, discount," +
                " amount, date_processed) values(?,?,?,?,?,?)";
        PreparedStatement statement = connection.prepareStatement(query,
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        statement.setInt(1, customerId);
        statement.setInt(2, productId);
        statement.setInt(3, quantity);
        statement.setDouble(4, discount);
        statement.setDouble(5, amount);
        statement.setDate(6, Date.valueOf(LocalDate.now()));
        statement.execute();
    }

    private void generateReceipt(int customerId, List<Order> orders) throws SQLException{
        // write the receipt to a file
    }

    public static void main(String[] args) throws SQLException {
        Main main = new Main();
        Scanner input = new Scanner(System.in);
        System.out.println("Welcome to ABC Supermarket, how may we serve you today?: ");
        System.out.println("1. Check items that need replenishment\n2. Check number of items left\n3.Replenish stock" +
                "\n4.Order an item");
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
            case 4:
                main.initiateOrder();
                break;


            default:
                System.out.println("Invalid choice!");
        }
    }
}
