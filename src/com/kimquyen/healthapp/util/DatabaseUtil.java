package com.kimquyen.healthapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.kimquyen.healthapp.config.DatabaseConfig; // Đảm bảo import này đúng

public class DatabaseUtil {
    static {
        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver đã được tải thành công!");
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy MySQL JDBC Driver!");
            e.printStackTrace();

            throw new RuntimeException("MySQL JDBC Driver not found!", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        System.out.println("Đang thử kết nối tới DB: " + DatabaseConfig.DB_URL);
        Connection connection = DriverManager.getConnection(
            DatabaseConfig.DB_URL,
            DatabaseConfig.DB_USER,
            DatabaseConfig.DB_PASSWORD
        );
        // Nếu không có exception nào được ném ra ở trên, kết nối đã thành công
        System.out.println("Kết nối Database THÀNH CÔNG!");
        return connection;
    }

    // Phương thức main để kiểm tra kết nối
    public static void main(String[] args) {
        Connection conn = null;
        try {
            System.out.println("--- Bắt đầu kiểm tra kết nối Database ---");
            conn = DatabaseUtil.getConnection(); 

            if (conn != null && !conn.isClosed()) {
                System.out.println("Trạng thái kết nối: Đang mở.");
            } else {
                System.err.println("Kết nối không thành công hoặc đã bị đóng.");
            }

        } catch (SQLException e) {
            System.err.println("Kết nối Database THẤT BẠI! Lỗi SQL:");
            e.printStackTrace(); // In chi tiết lỗi SQL
            System.err.println("Kiểm tra các thông tin sau:");
            System.err.println("1. MySQL Server có đang chạy không?");
            System.err.println("2. Thông tin trong DatabaseConfig.java (DB_URL, DB_USER, DB_PASSWORD) có chính xác không?");
            System.err.println("3. Database 'hihi' đã tồn tại trên MySQL Server chưa?");
            System.err.println("4. MySQL Connector/J JAR đã được thêm vào classpath của project chưa?");
            System.err.println("5. Firewall có chặn kết nối tới cổng MySQL (mặc định 3306) không?");
        } catch (RuntimeException e) { // Bắt RuntimeException từ static block nếu driver không tải được
             System.err.println("Lỗi nghiêm trọng khi khởi tạo DatabaseUtil:");
             e.printStackTrace();
        }
        finally {
            if (conn != null) {
                try {
                    conn.close(); 
                    System.out.println("Kết nối đã được đóng.");
                } catch (SQLException e) {
                    System.err.println("Lỗi khi đóng kết nối:");
                    e.printStackTrace();
                }
            }
            System.out.println("--- Kết thúc kiểm tra kết nối Database ---");
        }
    }
}