/**
* @Autor    :Xintong Ding
* @Data     :2018.10.30
* @Version  :0.1
* */

package ding;

import java.sql.*;

public class Database {

    public static final int MYSQL = 1;

    boolean db_link;
    Connection  db;
    Statement   st;
    ResultSet   rs;

    String str_db_type;
    String db_name;

    public static class Table {
        boolean isdata;
        String table_name;
        String data_str[][];
        String data_header[];
        String data_primary[];
        int data_rows;
        int data_cols;
        int data_primary_num;
        Statement st;
        ResultSet rs;
        ResultSetMetaData rsmd;
        DatabaseMetaData  dbmd;

        Table(){}

        boolean deleteRow(int row){
            try {
                rs.absolute(row);
                rs.deleteRow();
                refreshData();
                return true;
            }catch (Exception e){
                return false;
            }finally {

            }
        }

        boolean updateData(int row,int col,String data){
            try{
                rs.absolute(row);
                rs.updateString(col,data);
                rs.updateRow();
                return true;
            }catch (Exception e){
                return false;
            }finally {
            }
        }

        boolean insertData(String[] row_data){
            if(row_data != null) {
                try {
                    rs.moveToInsertRow();
                    for (int i = 0; i < row_data.length; i++) {
                        rs.updateString(i + 1, row_data[i]);
                    }
                    rs.insertRow();
                    return true;
                } catch (Exception e) {
                    return false;
                } finally {

                }
            }else {return false; }
        }

        boolean insertRow() {
            try{
                rs.moveToInsertRow();
                for(int i = 0;i<data_cols;i++) {
                    if(isPrimaryKey(data_header[i])){
                        rs.updateNull(data_header[i]);
                    }else {
                        rs.updateString(data_header[i],"0");
                    }
                }
                rs.insertRow();
                refreshData();
                return true;
            }catch (Exception e){return false;}
            finally { }
        }

        boolean closeTable(){
            try{
                rsmd = null;
                rs.close();
                st.close();
                return true;
            }catch (Exception e){
                return false;
            }finally {

            }
        }

        boolean refreshData(){
            try{
                rs.last();
                data_rows = rs.getRow();
                data_cols = rsmd.getColumnCount();

                data_header   = new String[data_cols];
                data_str      = new String[data_rows][data_cols];
                rs.beforeFirst();
                rs.next();
                for(int i = 0;i<data_rows;i++) {
                    for (int j = 1; j <= data_cols; j++) {
                        data_str[i][j-1] = rs.getString(j);
                        if(i == 0){
                            data_header[j-1] = rsmd.getColumnName(j);
                        }
                    }
                    rs.next();
                }
                return true;
            }catch (Exception  e){return false;}
        }

        void printData(){
            if(isdata){
                for(int i = 0;i<data_cols;i++){
                    System.out.print(data_header[i] + "\t");
                }
                System.out.println("");
                for(int i = 0;i<data_rows;i++){
                    for(int j = 0;j<data_cols;j++){
                        System.out.print(data_str[i][j] + "\t");
                    }
                    System.out.println("");
                }
            }
        }

        private boolean isPrimaryKey(String key){
            for(int i = 0;i < data_primary_num;i++){
                if(key.equals(data_primary[i]))return true;
            }
            return false;
        }
    }

    public static class SQLRequest{
        String sql;
        String table_name;
        SQLRequest(){}
        SQLRequest(String sql,String table_name){
            this.sql = sql;
            this.table_name = table_name;
        }
    }

    Database(){}

    Database(int flag, String db_name, String user, String password){
        dbLink(flag,db_name,user,password);
    }

    boolean dbLink(int flag, String db_name, String user, String password){
        this.db_name = db_name;
        str_db_type = new String();
        String str_link = new String();
        switch (flag){
            case 1:
                str_link = "com.mysql.cj.jdbc.Driver";
                str_db_type = "mysql";
                break;
            default:
                break;
        }
        try{
            Class.forName(str_link);
            db = DriverManager.getConnection("jdbc:" + str_db_type + "://localhost/" + db_name + "?useSSL=FALSE&serverTimezone=UTC",user,password);
            db_link = true;
            return db_link;
        }catch (Exception e){
            db_link = false;
            return db_link;
        }
        finally {

        }
    }

    Table dbSelect(SQLRequest sqlre){
        Table t = new Table();
        t.isdata = false;
        ResultSetMetaData rsmd;
        DatabaseMetaData dbmd;
        try{
            st = db.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE ,ResultSet.CONCUR_UPDATABLE);
            dbmd = db.getMetaData();
            rs = dbmd.getPrimaryKeys(null,null,sqlre.table_name);
            rs.last();
            t.data_primary_num = rs.getRow();
            t.data_primary = new String[t.data_primary_num];
            t.table_name = sqlre.table_name;
            rs.beforeFirst();
            rs.next();
            for(int i = 0; i < t.data_primary_num; i++){
                t.data_primary[i] = rs.getString("COLUMN_NAME");
                rs.next();
            }

            rs = st.executeQuery(sqlre.sql);
            rsmd = rs.getMetaData();

            rs.last();
            t.data_rows = rs.getRow();
            t.data_cols = rsmd.getColumnCount();

            t.data_header   = new String[t.data_cols];
            t.data_str      = new String[t.data_rows][t.data_cols];

            rs.beforeFirst();
            rs.next();
            for(int i = 0;i<t.data_rows;i++) {
                for (int j = 1; j <= t.data_cols; j++) {
                    t.data_str[i][j-1] = rs.getString(j);
                    if(i == 0){
                        t.data_header[j-1] = rsmd.getColumnName(j);
                    }
                }
                rs.next();
            }
            t.rsmd = rsmd;
            t.dbmd = dbmd;
            t.rs = rs;
            t.st = st;
            t.isdata = true;
            st = null;
            rs = null;
        }catch (Exception e)
        {
        }finally {
            return t;
        }
    }

    int dbUpdate(String sql){
        int temp = -1;
        try{
            st = db.createStatement();
            temp = st.executeUpdate(sql);
        }
        catch (Exception e){

        }finally {
            return temp;
        }
    }

    public static SQLRequest toStrSQLSelectTable(String table_name){
        return  new SQLRequest("select * from "+ table_name,table_name);
    }

    public static String toStrSQLSelectTable(String table_name,String col_name[]){
        String temp = new String();

        for(int i = 0;i<col_name.length-1;i++){
            temp += (col_name[i] + ",");
        }
        temp += col_name[col_name.length-1];

        return "select " + temp + " from " + table_name;
    }
}
