/**
 * @Autor    :Xintong Ding
 * @Data     :2018.10.30
 * @Version  :0.1
 * */
package ding;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WinMain{
    Database one;
    Database.Table user;

    JFrame      jf_main;
    JMenuBar    munubar_main;
    JMenu       menu_file, menu_database, menu_table, menu_help;

    /*文件*/
    JMenuItem   menuItem_exit;

    /*数据库*/
    JMenuItem   menuItem_link,
                menuItem_break;

    /*表*/
    JMenuItem   menuItem_teacher,
                menuItem_student,
                menuItem_user,
                menuItem_score;

    /*关于*/
    JMenuItem   menuItem_about;


    JDesktopPane desktopPane;

    JInternalFrame internalFrame;

    private int limit = -1;

    WinMain(){
        jf_main.setContentPane(desktopPane);
        jf_main.setVisible(true);
        one = new Database(Database.MYSQL,"test","root","ding");
        user = one.dbSelect(Database.toStrSQLSelectTable("user"));
    }

    protected JInternalFrame createEnterWin(){
        internalFrame = WinMain.createInternalFrame("登入",300,150);

        internalFrame.setLayout(new GridLayout(3,2));

        //JLabel      label_db_name    = new JLabel("数据库名：");
        //JTextField  db_name_in       = new JTextField(30);
        JLabel      label_user       = new JLabel("用户名：");
        JTextField  user_in          = new JTextField(30);
        JLabel      label_pawd       = new JLabel("密码  ：");
        JTextField  pawd_in          = new JTextField(30);
        JButton     button_link      = new JButton("登入");

        button_link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                for(int i = 0;i < user.data_str.length;i++){
                    if(user.data_str[i][1].equals(user_in.getText())){
                        if(user.data_str[i][2].equals(pawd_in.getText())){
                            limit = Integer.valueOf(user.data_str[i][3]);
                            JOptionPane.showMessageDialog(null,"登入成功，用户名：" + user.data_str[i][1]);
                            internalFrame.setVisible(false);
                            internalFrame = null;
                        }else{
                            limit = 0;
                            JOptionPane.showMessageDialog(null,"密码错误");
                        }
                        break;
                    }

                }
                if(limit == -1){
                    JOptionPane.showMessageDialog(null,"无次用户");
                }
            }
        });

        internalFrame.add(label_user);
        internalFrame.add(user_in);
        internalFrame.add(label_pawd);
        internalFrame.add(pawd_in);
        internalFrame.add(button_link);
        internalFrame.setVisible(true);

        updateDisplay(internalFrame);
        return internalFrame;
    }

    protected JInternalFrame showTable(String title,int lim){
        if(one != null){
            updateDisplay(createShowTableWin(title,lim));

            return internalFrame;
        }else{
            JOptionPane.showMessageDialog(null,"请先连接数据库");
            return null;
        }
    }

    protected JInternalFrame createShowTableWin(String table_name,int lim){
        Database.Table t = one.dbSelect(Database.toStrSQLSelectTable(table_name));
        if(!t.isdata) {
            return null;
        }else{
            JInternalFrame internalFrame_result = WinMain.createInternalFrame(t.table_name,600,800);
            t.table_name = table_name;
            JTable table = new JTable(t.data_str,t.data_header);
            TableModel tableModel = table.getModel();
            DefaultTableModel defaultTableModel = new DefaultTableModel(t.data_str,t.data_header);
            table.setModel(defaultTableModel);

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    table.updateUI();
                    if(e.getButton() == 3){
                        JPopupMenu temp_popupMenu = new JPopupMenu();
                        JMenuItem  temp_menuItem_delete = new JMenuItem("删除");
                        JMenuItem  temp_menuItem_insert = new JMenuItem("增添");
                        temp_popupMenu.add(temp_menuItem_delete);
                        temp_popupMenu.add(temp_menuItem_insert);
                        temp_popupMenu.show(table,e.getX(),e.getY());

                        temp_menuItem_delete.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mousePressed(MouseEvent e) {
                                super.mouseClicked(e);
                                int row_temp = table.getSelectedRow();
                                if(row_temp >= 0){
                                    if(!t.deleteRow(row_temp+1)){
                                        System.out.println("deleteRow failed");
                                        JOptionPane.showMessageDialog(null,"删除行失败");
                                    }else{
                                        defaultTableModel.removeRow(row_temp);
                                        defaultTableModel.fireTableDataChanged();
                                    }
                                }
                            }
                        });

                        temp_menuItem_insert.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mousePressed(MouseEvent e) {
                                super.mouseClicked(e);
                                String temp[] = new String[t.data_cols];
                                for(int i=0;i<t.data_cols;i++){
                                    temp[i] = "0";
                                }
                                t.insertRow();
                                defaultTableModel.addRow(t.data_str[t.data_rows - 1]);
                                defaultTableModel.fireTableDataChanged();
                            }
                        });

                    }
                }
            });

            defaultTableModel.addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent e) {
                    if(e.getColumn() >= 0){
                        if(!t.updateData(e.getFirstRow()+1,e.getColumn()+1,(String)defaultTableModel.getValueAt(e.getFirstRow(),e.getColumn()))){
                            JOptionPane.showMessageDialog(null,"修改失败，请重新输入");
                            System.out.println("失败");
                        }
                    }
                }
            });

            internalFrame_result.add(table.getTableHeader(),BorderLayout.NORTH);
            internalFrame_result.add(table,BorderLayout.CENTER);
            internalFrame_result.setVisible(true);
            return internalFrame_result;
        }
    }

    private static  JInternalFrame createInternalFrame(String title,int width,int height){
        JInternalFrame internalFrame_temp = new JInternalFrame(title,true,true,true,true);
        internalFrame_temp.setSize(width,height);
        return internalFrame_temp;
    }

    private boolean updateDisplay(JInternalFrame internalFrame_in){
        if(internalFrame_in != null){
            desktopPane.add(internalFrame_in);
            desktopPane.revalidate();
            jf_main.revalidate();
            try{
                internalFrame_in.setSelected(true);
                return true;
            }catch (Exception e1){
                return false;
            }
        } else{
            return false;
        }
    }

    private void    setMainUI(){
        jf_main = new JFrame("简易数据库客户端");

        munubar_main = new JMenuBar();

        menu_file       = new JMenu("文件");
        menu_database   = new JMenu("登入");
        menu_table      = new JMenu("管理");
        menu_help      = new JMenu("帮助");

        menuItem_exit   = new JMenuItem("退出");
        menuItem_link   = new JMenuItem("登入");
        menuItem_break  = new JMenuItem("断开连接");
        menuItem_teacher = new JMenuItem("老师管理");
        menuItem_student = new JMenuItem("学生管理");
        menuItem_user = new JMenuItem("用户管理");
        menuItem_score = new JMenuItem("分数管理");
        menuItem_about  = new JMenuItem("关于");

        desktopPane = new JDesktopPane();

        munubar_main.add(menu_file);
        munubar_main.add(menu_database);
        munubar_main.add(menu_table);
        munubar_main.add(menu_help);

        menu_file.add(menuItem_exit);
        menu_database.add(menuItem_link);
        menu_database.add(menuItem_break);
        menu_table.add(menuItem_teacher);
        menu_table.add(menuItem_student);
        menu_table.add(menuItem_user);
        //menu_table.add(menuItem_score);
        menu_help.add(menuItem_about);

        jf_main.setJMenuBar(munubar_main);
        //jf_main.setIconImage();
        jf_main.pack();
        jf_main.setSize(1280,720);
        jf_main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf_main.setLayout(new BorderLayout());
    }

    private void    addMainListener(){
        /*退出*/
        menuItem_exit.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                System.exit(1);
            }
        });

        /*连接数据库*/
        menuItem_link.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                createEnterWin();
            }
        });


        /*老师管理*/
        menuItem_teacher.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if(limit >= 2)
                    updateDisplay(showTable("teacher",2));
                else
                    JOptionPane.showMessageDialog(null,"权限不够");
            }
        });

        /*学生管理*/
        menuItem_student.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if(limit >= 1)
                    updateDisplay(showTable("student",2));
                else
                    JOptionPane.showMessageDialog(null,"权限不够");

            }
        });

        /*用户管理*/
        menuItem_user.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if(limit >= 3)
                    updateDisplay(showTable("user",2));
                else
                    JOptionPane.showMessageDialog(null,"权限不够");
            }
        });

        /*关于*/
        menuItem_about.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                JOptionPane.showMessageDialog(null,"作者：教务管理","关于",JOptionPane.PLAIN_MESSAGE);
            }
        });
    }

    {
        setMainUI();
        addMainListener();
    }
}
