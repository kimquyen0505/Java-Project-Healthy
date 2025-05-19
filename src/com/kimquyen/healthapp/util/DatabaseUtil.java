package com.kimquyen.healthapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.kimquyen.healthapp.config.DatabaseConfig; // Đảm bảo import này đúng

public class DatabaseUtil {
    static {
        try {
            // Đảm bảo tên driver chính xác cho phiên bản MySQL Connector/J bạn đang dùng
            // Ví dụ: com.mysql.cj.jdbc.Driver cho phiên bản 8+
            // Hoặc: com.mysql.jdbc.Driver cho phiên bản 5.x
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver đã được tải thành công!");
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy MySQL JDBC Driver!");
            e.printStackTrace();
            // Ném RuntimeException để dừng ứng dụng nếu driver không tải được
            // vì đây là lỗi nghiêm trọng không thể tiếp tục.
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
            conn = DatabaseUtil.getConnection(); // Gọi phương thức getConnection

            if (conn != null && !conn.isClosed()) {
                System.out.println("Trạng thái kết nối: Đang mở.");
                // Bạn có thể thực hiện một truy vấn đơn giản ở đây nếu muốn
                // ví dụ: conn.getMetaData().getDatabaseProductName();
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
                    conn.close(); // Luôn đóng kết nối sau khi sử dụng
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