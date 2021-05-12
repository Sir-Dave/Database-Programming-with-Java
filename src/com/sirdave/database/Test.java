package com.sirdave.database;

public class Test {
    String name = "Sales Invoice for ....................\t\t\tDate: 26/6/2015\n" +
            "\n" +
            "S/N\t\tItem\t\tNumber of\t\tUnit Price\t\tDiscount\t\tTotal Payable\n";

    String names =
            "\t\tPackets Bought    \tper packet\t     Given\n" +
            "\n" +
            "1\tMilo\t       7\t\t1000\t       \t        350\t\t 6650.00\n" +
            "2\tSugar\t       4\t\t  300              \t         0                \t 1200.00  \n" +
            "\n" +
            "\t\t\t\t\tTotal Discount Given:     350.00\n" +
            "\t\t\t\t\tNet Payable:\t         7850.00\n" +
            "\n" +
            "……………………………….\n" +
            "Manager\n" +
            "Thanks for your patronage\n";


    public static void main(String[] args){
        Test test = new Test();
        System.out.println(test.name);
    }
}
