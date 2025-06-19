package com.project.demo001;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnectionTest {
	public static void main(String[] args) {
		
		(가림)
		
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("✅ MySQL 연결 성공!");
            conn.close();
        } catch (SQLException e) {
            System.out.println("❌ MySQL 연결 실패...");
            e.printStackTrace();
        }
		
	}
}
