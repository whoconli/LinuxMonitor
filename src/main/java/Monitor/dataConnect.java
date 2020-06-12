package Monitor;

import java.sql.*;

public class dataConnect {
    private ResultSet rs;
    private Statement stmt;
    private Connection conn;

//    public static void main(String[] args) throws SQLException, ClassNotFoundException {
//        //注册驱动
//        Class.forName("com.mysql.cj.jdbc.Driver");
//        //创建连接
//        Connection conn=DriverManager.getConnection
//                ("jdbc:mysql://localhost/mydata?useSSL=FALSE&serverTimezone=UTC","conli","12345");
//        //得到执行sql语句的Statement对象
//        Statement stmt=conn.createStatement();
//        //执行sql语句，并返回结果
//        rs = stmt.executeQuery("select * from monitorData");
//        //处理结果
//        while (rs.next()) {
//            for (int i = 1; i <= 5; i++) {
//                System.out.print(rs.getString(i) + "\t");
//            }
//            System.out.println();
//        }
//        float  cpu=1,mem=2,io=3,pack=4;
//
//        String insert = "insert into monitorData(cpuUtil,memUtil,ioUtil,packets) values('"+cpu+"','"+mem+"','"+io+"','"+pack+"')";
//        String drop = "truncate table monitorData";
//        String select = "SELECT * FROM monitorData order by id DESC limit " + 5;
////        System.out.println(select);
//        stmt.executeUpdate(insert);
//
//        rs=stmt.executeQuery("select * from monitorData");
//
//        //处理结果
//        while (rs.next()) {
//            for (int i = 1; i <= 5; i++) {
//                System.out.print(rs.getString(i) + "\t");
//            }
//            System.out.println();
//        }
//
//        //关闭资源
//        rs.close();
//        stmt.close();
//        conn.close();
//    }

    public Statement getConnect() throws SQLException, ClassNotFoundException {
        //注册驱动
        Class.forName("com.mysql.cj.jdbc.Driver");
        //创建连接
        conn=DriverManager.getConnection
                ("jdbc:mysql://localhost/mydata?useSSL=FALSE&serverTimezone=UTC","conli","12345");
        //得到执行sql语句的Statement对象
        stmt=conn.createStatement();
        return stmt;
    }

    public void destory() throws SQLException {
        rs.close();
        stmt.close();
        conn.close();
    }
}
