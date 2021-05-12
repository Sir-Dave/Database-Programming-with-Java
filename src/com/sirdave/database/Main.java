package com.sirdave.database;

import com.sirdave.database.Models.*;

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
        String query = "select * from inventory where lower(product_name)=?";
        PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setString(1, item.toLowerCase());
        ResultSet resultSet = statement.executeQuery();
        Inventory inventory = new Inventory();

        while (resultSet.next()){    // because we want to get the most recent inventory
            int id = resultSet.getInt("product_id");
            String name = resultSet.getString("product_name");
            int quantity = resultSet.getInt("quantity");
            inventory = new Inventory(id, name, quantity);
        }

        return inventory.getQuantity();
    }

    // helper function to automatically update the quantity of a selected item
    private void updateItem(String item, int quantity) throws SQLException {
        String query = "update inventory set quantity=? where lower(product_name)=?";
        PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setInt(1, quantity);
        statement.setString(2, item.toLowerCase());
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

    private int getCustomerIdByName(String name) throws SQLException {
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

    private String findCustomerById(int id) throws SQLException {
        String query = "select * from customers where customer_id=?";
        PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setInt(1, id);
        ResultSet resultSet = statement.executeQuery();
        resultSet.first(); // because there can only be one user with that id
        int customerId = resultSet.getInt("customer_id");
        String customerName = resultSet.getString("customer_name");
        Customer customer = new Customer(customerId, customerName);
        return customer.getName();
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

    //overloaded function
    private Product getProductDetails(int productId) throws SQLException {
        String query = "select * from products where id=?";
        PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setInt(1, productId);
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

            userId = getCustomerIdByName(userName);
        }
        catch (SQLException e){
            addNewCustomer(userName);
            userId = getCustomerIdByName(userName);
        }

        char choice;
        do{
            System.out.println("Please enter the item you want to buy: ");
            String itemName = input.next();

            if (isProductExists(itemName)){
                System.out.println("How many " + itemName+ "(s) do you want to order?: ");
                quantity = input.nextInt();
                Order order = new Order(itemName, quantity, LocalDate.now());
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

        List<Sale> allSales = generateSalesData(userId, LocalDate.now());
        String customerName = findCustomerById(userId);
        StringBuilder receipt = generateSalesReceipt(customerName, allSales);
        System.out.println(receipt);
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

            // update the current number of products in the database
            int previousNum = checkNumItem(name);
            updateItem(product.getName(), previousNum - quantity);
        }
    }

    // method for querying the data to be generated as a receipt
     private List<Sale> generateSalesData(int customerId, LocalDate localDate) throws SQLException {
        ArrayList<Sale> sales = new ArrayList<>();
        String query = "select * from sales where customer_id = ? and date_processed = ?";
        PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        statement.setInt(1, customerId);
        statement.setDate(2, Date.valueOf(localDate));
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()){
            int salesId = resultSet.getInt("sales_id");
            int userId = resultSet.getInt("customer_id");
            int productId = resultSet.getInt("product_id");
            int quantity = resultSet.getInt("quantity");
            double discount = resultSet.getInt("discount");
            double amount = resultSet.getInt("amount");
            Date date = resultSet.getDate("date_processed");

            Sale sale = new Sale(salesId, userId, productId, quantity,
                    discount, amount, date.toLocalDate());

            sales.add(sale);
        }
        return sales;
    }


    // generates the receipt
    private StringBuilder generateSalesReceipt(String customerName, List<Sale> sales) throws SQLException {
        StringBuilder receipt = new StringBuilder("Sales Invoice for " + customerName + " \t\t\t\tDate: " + sales.get(0).getDate() +
                "\n\n" +
                "S/N\t\tItem\t\tNumber of Packets bought\t\tUnit Price\t\tDiscount\t\tTotal Payable\n");

        double totalDiscount = 0, totalAmount = 0;

        for (Sale sale: sales){
            Product product = getProductDetails(sale.getProductId());
            receipt.append(sales.indexOf(sale) + 1).append("\t\t").append(product.getName())
                    .append("\t\t").append(sale.getQuantity()).append("\t\t\t\t\t\t\t\t").
                    append(product.getPrice()).append("\t\t\t").append(sale.getDiscount()).
                    append("\t\t\t").append(sale.getAmount()).append("\n");

            totalAmount+= sale.getAmount();
            totalDiscount += sale.getDiscount();
        }

        receipt.append("\t\t\t\t\t\tTotal Discount Given: ").append(totalDiscount).append("\n");
        receipt.append("\t\t\t\t\t\tNet Payable: ").append(totalAmount).append("\n");
        receipt.append("\t\t\t\t\t\t................ ").append("\n");
        receipt.append("\t\t\t\t\t\tManager").append("\n");
        receipt.append("\t\t\t\t\t\tThanks for your patronage").append("\n");

        return receipt;
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
                ArrayList<Inventory> inventories = main.checkItemsNeedReplenishment();
                if (inventories.isEmpty()) {
                    System.out.println("No item needs to be replenished for now");
                }
                else {
                    System.out.println("The following items are running out of stock: ");
                    for(Inventory inventory: inventories){
                        System.out.println(inventory.getName() + " - " + inventory.getQuantity() + " packets");
                    }
                }

                break;

            case 2: {
                System.out.println( "Enter the name of the item you want to check for: ");
                String item = input.next();
                int num = main.checkNumItem(item);
                System.out.println(num);
                break;
            }
            case 3:{
                System.out.println( "Enter the name of the item you want to update: ");
                String item = input.next();
                int currentQuantity = main.checkNumItem(item);
                main.replenishStock(item, currentQuantity);
                System.out.println("Item(s) updated");
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
