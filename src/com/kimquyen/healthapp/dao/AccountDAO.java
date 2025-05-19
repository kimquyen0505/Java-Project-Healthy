package com.kimquyen.healthapp.dao;

import com.kimquyen.healthapp.model.Account;
import com.kimquyen.healthapp.model.Role;
import com.kimquyen.healthapp.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    public Account getAccountByUsername(String username) {
        String sql = "SELECT id, username, password, role, user_data_fk_id FROM account WHERE username = ?"; // Lấy cả id của account
        Account account = null;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String roleStringDb = rs.getString("role");
                Role accountRole = Role.fromString(roleStringDb);

                if (accountRole == null && roleStringDb != null && !roleStringDb.trim().isEmpty()) {
                    System.err.println("Cảnh báo DAO: Giá trị role không hợp lệ ('" + roleStringDb + "') từ DB cho username: " + username + ". Account role sẽ là null.");
                } else if (accountRole == null) {
                     System.err.println("Cảnh báo DAO: Giá trị role là NULL hoặc rỗng từ DB cho username: " + username + ". Account role sẽ là null.");
                }

                account = new Account(
                    rs.getString("username"),
                    rs.getString("password"),
                    accountRole,
                    rs.getInt("user_data_fk_id") // Đây là ID của UserData
                );
                // Tùy chọn: Nếu bạn muốn lưu ID của bản ghi account (khóa chính của bảng account)
                // vào đối tượng Account (ví dụ: model Account có thêm trường accountId), bạn có thể làm:
                // account.setAccountId(rs.getInt("id")); // Giả sử Account model có setAccountId()
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy Account theo username: " + username);
            e.printStackTrace();
        }
        return account;
    }

    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT id, username, password, role, user_data_fk_id FROM account"; // Lấy cả id của account
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String roleStringDb = rs.getString("role");
                Role accountRole = Role.fromString(roleStringDb);

                if (accountRole == null && roleStringDb != null && !roleStringDb.trim().isEmpty()) {
                     System.err.println("Cảnh báo DAO (getAll): Giá trị role không hợp lệ ('" + roleStringDb + "') cho username: " + rs.getString("username") + ". Account role sẽ là null.");
                } else if (accountRole == null) {
                     System.err.println("Cảnh báo DAO (getAll): Giá trị role là NULL hoặc rỗng cho username: " + rs.getString("username") + ". Account role sẽ là null.");
                }

                Account account = new Account(
                    rs.getString("username"),
                    rs.getString("password"),
                    accountRole,
                    rs.getInt("user_data_fk_id") // ID của UserData
                );
                // account.setAccountId(rs.getInt("id")); // Nếu cần lưu ID của bản ghi account
                accounts.add(account);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả Accounts");
            e.printStackTrace();
        }
        return accounts;
    }

    // << SỬA PHƯƠNG THỨC NÀY >>
    public boolean addAccount(Account account) {
        // Câu lệnh SQL bây giờ phải bao gồm user_data_fk_id
        String sql = "INSERT INTO account (username, password, role, user_data_fk_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, account.getUsername());
            pstmt.setString(2, account.getPassword()); // Mật khẩu đã băm

            if (account.getRole() != null) {
                pstmt.setString(3, account.getRole().name()); // Lưu tên Enum (ADMIN, USER)
            } else {
                pstmt.setNull(3, Types.VARCHAR); // Hoặc một giá trị mặc định nếu role không được null trong DB
                System.err.println("Cảnh báo DAO: Thêm Account với Role là null cho username: " + account.getUsername());
            }
            // Đảm bảo account.getUserId() trả về ID của UserData liên quan
            if (account.getUserId() != 0) { // ID 0 có thể coi là không hợp lệ hoặc không có liên kết
                pstmt.setInt(4, account.getUserId());
            } else {
                pstmt.setNull(4, Types.INTEGER); // Nếu userId là 0, có thể bạn muốn lưu NULL
                System.err.println("Cảnh báo DAO: Thêm Account với user_data_fk_id là 0/NULL cho username: " + account.getUsername());
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException | NullPointerException e) { // Bắt cả NullPointerException nếu account.getRole() là null và không kiểm tra
            System.err.println("Lỗi khi thêm Account: " + (account != null ? account.getUsername() : "Đối tượng Account là null"));
            e.printStackTrace();
            return false;
        }
    }

    // << SỬA PHƯƠNG THỨC NÀY (NẾU CẦN CẬP NHẬT user_data_fk_id) >>
    public boolean updateAccount(Account account) {
        // Nếu bạn KHÔNG BAO GIỜ thay đổi user_data_fk_id sau khi tạo, câu SQL cũ vẫn ổn.
        // Nếu bạn CÓ THỂ thay đổi user_data_fk_id, cần thêm nó vào câu SQL.
        // Hiện tại, giả sử chỉ cập nhật password và role.
        String sql = "UPDATE account SET password = ?, role = ? WHERE username = ?";
        // Nếu muốn cập nhật cả user_data_fk_id:
        // String sql = "UPDATE account SET password = ?, role = ?, user_data_fk_id = ? WHERE username = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, account.getPassword()); // Mật khẩu đã băm
            if (account.getRole() != null) {
                pstmt.setString(2, account.getRole().name());
            } else {
                pstmt.setNull(2, Types.VARCHAR);
            }
            // Nếu muốn cập nhật user_data_fk_id:
            // if (account.getUserId() != 0) {
            //     pstmt.setInt(3, account.getUserId());
            // } else {
            //     pstmt.setNull(3, Types.INTEGER);
            // }
            // pstmt.setString(CHỈ_SỐ_CỦA_USERNAME, account.getUsername()); // Chỉ số sẽ thay đổi nếu thêm user_data_fk_id
            pstmt.setString(3, account.getUsername());


            return pstmt.executeUpdate() > 0;
        } catch (SQLException | NullPointerException e) {
            System.err.println("Lỗi khi cập nhật Account: " + (account != null ? account.getUsername() : "Đối tượng Account là null"));
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAccount(String username) {
        String sql = "DELETE FROM account WHERE username = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa Account: " + username);
            e.printStackTrace();
            return false;
        }
    }

    // Phương thức lấy Account bằng UserData ID (khóa ngoại user_data_fk_id)
    // (Đã thêm ở câu trả lời trước, đảm bảo nó tồn tại và đúng)
    public Account getAccountByUserId(int userId) {
        String sql = "SELECT id, username, password, role, user_data_fk_id FROM account WHERE user_data_fk_id = ?";
        Account account = null;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String roleStringDb = rs.getString("role");
                Role accountRole = Role.fromString(roleStringDb);
                // ... (xử lý roleStringDb và accountRole null như trên) ...
                account = new Account(
                    rs.getString("username"),
                    rs.getString("password"),
                    accountRole,
                    rs.getInt("user_data_fk_id")
                );
                // account.setAccountId(rs.getInt("id")); // Nếu cần
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy Account theo UserData ID: " + userId);
            e.printStackTrace();
        }
        return account;
    }
}