package com.comutil.sql;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MysqlTableScriptPrinter is a class designed to connect to a MySQL database,
 * retrieve table information, and print the DDL (Data Definition Language) scripts
 * for each table. It also generates Java Bean and Go Struct representations of the tables.
 */
public class MysqlTableScriptPrinter {

    public static void main(String[] args) {
        if (args.length < 5) {
            System.err.println("Usage: java TableDdlExporter <host> <port> <database> <user> <password> <tables...>");
            System.exit(1);
        }

        String host = args[0];
        String port = args[1];
        String database = args[2];
        String user = args[3];
        String password = args[4];

        List<String> tables = new ArrayList<>();    // 存储要导出的表名
        for (int i = 5; i < args.length; i++) {
            tables.add(args[i]);
        }    // 存储要导出的表名

        Connection conn = null;
        try {
            // 加载JDBC驱动
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 构建连接URL
            String url = String.format(
                    "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    host, port, database
            );

            // 建立数据库连接
            conn = DriverManager.getConnection(url, user, password);

            // 获取所有表名
            if (tables.isEmpty()) {
                tables = getAllTables(conn, database);
            }

            // 生成并输出每个表的DDL
            for (String table : tables) {
                String ddl = getTableDDL(conn, table);
                System.out.println(ddl + ";\n");

                System.out.println("\n==== java bean ====\n");
                String javaBean = new SqlToJavaBeanConverter().convertToJavaBean(ddl);
                System.out.println(javaBean);

                System.out.println("\n==== go struct ====\n");
                String goBean = new SqlToGoBeanConverter().convertToGoBean(ddl);
                System.out.println(goBean);
            }

        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection error:");
            e.printStackTrace();
        } finally {
            // 关闭数据库连接
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection:");
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<String> getAllTables(Connection conn, String dbName) throws SQLException {
        List<String> tables = new ArrayList<>();
        DatabaseMetaData meta = conn.getMetaData();

        try (ResultSet rs = meta.getTables(dbName, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }
        return tables;
    }

    public static String getTableDDL(Connection conn, String tableName) throws SQLException {
        String sql = "SHOW CREATE TABLE `" + tableName + "`";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getString("Create Table");
            }
        }
        throw new SQLException("No DDL found for table: " + tableName);
    }
}
