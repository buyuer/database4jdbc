package ding;

import javax.swing.*;

public class Main {

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        WinMain win = new WinMain();

        System.out.println("Exit");

        //Database db = new Database(Database.MYSQL,"mysql_test","root","ding");

        //Database.Table t = db.dbSelect(Database.toStrSQLSelectTable("students"));

        //t.insertRow();


        //String data[] = {"123","刘亚飞","女","20","123123"};
        /*if(!t.insertData(data)){
            System.out.println("失败");
        }*/

        //t.deleteRow(6);

        /*if (!t.updateData(6,4,"21")){
            System.out.println("失败");
        }*/

        //t.deleteRow(1);

        //t.printData();
    }

}
