// package com.kimquyen.healthapp.service;
package com.kimquyen.healthapp.service;

import com.kimquyen.healthapp.util.ValidationUtil; // Import ValidationUtil
import com.kimquyen.healthapp.dao.AccountDAO;
import com.kimquyen.healthapp.dao.UserDataDAO;
// import com.kimquyen.healthapp.dao.SponsorDAO; // Thêm nếu cần kiểm tra sponsorId
import com.kimquyen.healthapp.model.Account;
import com.kimquyen.healthapp.model.Role;
import com.kimquyen.healthapp.model.UserData;
// import com.kimquyen.healthapp.model.Sponsor; // Thêm nếu cần

import java.sql.Timestamp;
// import java.sql.Connection; // Cho ví dụ transaction
// import java.sql.SQLException; // Cho ví dụ transaction
// import com.kimquyen.healthapp.util.DatabaseUtil; // Cho ví dụ transaction

public class AuthService {
    private final AccountDAO accountDAO;
    private final UserDataDAO userDataDAO;
    private final PasswordHashingService passwordHashingService;
    // private final SponsorDAO sponsorDAO; // Nếu cần kiểm tra sponsor

    public AuthService(AccountDAO accountDAO, UserDataDAO userDataDAO, PasswordHashingService passwordHashingService
                       /*, SponsorDAO sponsorDAO */) {
        if (accountDAO == null || userDataDAO == null || passwordHashingService == null) {
            throw new IllegalArgumentException("Các DAO và PasswordHashingService không được null khi khởi tạo AuthService.");
        }
        this.accountDAO = accountDAO;
        this.userDataDAO = userDataDAO;
        this.passwordHashingService = passwordHashingService;
        // this.sponsorDAO = sponsorDAO;
    }

    public Account login(String username, String plainPassword) {
        if (ValidationUtil.isNullOrEmpty(username) || ValidationUtil.isNullOrEmpty(plainPassword)) {
            System.err.println("SERVICE (login): Tên đăng nhập hoặc mật khẩu không được để trống.");
            return null;
        }

        Account account = accountDAO.getAccountByUsername(username);
        if (account != null) {
            if (passwordHashingService.checkPassword(plainPassword, account.getPassword())) {
                System.out.println("SERVICE (login): Đăng nhập thành công cho user: " + username);
                return account;
            } else {
                System.err.println("SERVICE (login): Sai mật khẩu cho người dùng: " + username);
            }
        } else {
            System.err.println("SERVICE (login): Không tìm thấy người dùng: " + username);
        }
        return null;
    }

    /**
     * Đăng ký người dùng mới.
     * Tạo UserData trước, sau đó tạo Account liên kết.
     * Mật khẩu sẽ được băm trước khi lưu.
     * Vai trò mặc định cho người dùng mới là USER.
     * @return true nếu đăng ký thành công, false nếu thất bại.
     */
    public boolean registerUser(String username, String plainPassword, String name, Integer sponsorId) {
        // 1. Validation đầu vào
        if (ValidationUtil.isNullOrEmpty(username) ||
            ValidationUtil.isNullOrEmpty(plainPassword) ||
            ValidationUtil.isNullOrEmpty(name)) {
            System.err.println("SERVICE (registerUser): Tên đăng nhập, mật khẩu, và tên không được để trống.");
            return false;
        }
        // Ví dụ thêm validation cho username và password
        if (!ValidationUtil.isValidUsername(username)) { // Giả sử có quy tắc cho username, ví dụ 3-20 ký tự, chữ và số
             System.err.println("SERVICE (registerUser): Định dạng tên đăng nhập không hợp lệ (ví dụ: 3-20 ký tự, chỉ chữ cái, số, dấu gạch dưới).");
             return false;
        }
        if (!ValidationUtil.isValidPassword(plainPassword, 8)) { // Ví dụ: mật khẩu tối thiểu 8 ký tự
             System.err.println("SERVICE (registerUser): Mật khẩu phải có ít nhất 8 ký tự.");
             return false;
        }
        // (Tùy chọn) Kiểm tra sự tồn tại của sponsorId nếu được cung cấp và khác 0
        // if (sponsorId != null && sponsorId != 0) {
        //     if (sponsorDAO.getSponsorById(sponsorId) == null) { // Cần có SponsorDAO và phương thức getSponsorById
        //         System.err.println("SERVICE (registerUser): Sponsor ID '" + sponsorId + "' không tồn tại.");
        //         return false;
        //     }
        // }

        // 2. Kiểm tra xem username đã tồn tại chưa
        if (accountDAO.getAccountByUsername(username) != null) {
            System.err.println("SERVICE (registerUser): Tên đăng nhập '" + username + "' đã tồn tại.");
            return false;
        }

        // ----- BẮT ĐẦU KHỐI LOGIC CẦN CÂN NHẮC TRANSACTION -----
        // Trong ứng dụng thực tế, bạn sẽ bắt đầu transaction ở đây
        // Connection conn = null;
        // try {
        //     conn = DatabaseUtil.getConnection();
        //     conn.setAutoCommit(false); // Bắt đầu transaction

        // 3. Tạo UserData
        UserData newUserInfo = new UserData();
        newUserInfo.setName(name);
        if (sponsorId != null && sponsorId != 0) { // Chỉ set nếu sponsorId hợp lệ và khác 0 (0 có thể là không có sponsor)
             newUserInfo.setSponsorId(sponsorId);
        } else {
            newUserInfo.setSponsorId(0); // Hoặc để null nếu DB cho phép và logic của bạn xử lý null
        }
        newUserInfo.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        UserData createdUserData = userDataDAO.addUser(newUserInfo); // userDataDAO.addUser(newUserInfo, conn);
        if (createdUserData == null || createdUserData.getId() == 0) {
            System.err.println("SERVICE ERROR (registerUser): Không thể tạo UserData. Kiểm tra log của UserDataDAO (có thể lỗi SQL, ràng buộc DB).");
            // if (conn != null) conn.rollback();
            return false;
        }
        System.out.println("SERVICE INFO (registerUser): UserData đã tạo với ID: " + createdUserData.getId() + " cho tên: " + createdUserData.getName());

        // 4. Băm mật khẩu
        String hashedPassword = passwordHashingService.hashPassword(plainPassword);

        // 5. Tạo Account
        Account newAccount = new Account(username, hashedPassword, Role.USER, createdUserData.getId()); // Truyền userId từ UserData đã tạo

        System.out.println("SERVICE INFO (registerUser): Đang tạo Account cho username: '" + username + "' với UserData ID: " + newAccount.getUserId());
        boolean accountCreated = accountDAO.addAccount(newAccount); // accountDAO.addAccount(newAccount, conn);

        if (!accountCreated) {
            System.err.println("SERVICE ERROR (registerUser): Không thể tạo Account. Kiểm tra log của AccountDAO. Đang cố gắng rollback UserData...");
            // Cố gắng rollback UserData (xóa UserData vừa tạo)
            // Đây là một nỗ lực rollback đơn giản, transaction thực sự sẽ an toàn hơn.
            boolean userDeleted = userDataDAO.deleteUser(createdUserData.getId()); // userDataDAO.deleteUser(createdUserData.getId(), conn);
            if (!userDeleted) {
                System.err.println("SERVICE ROLLBACK CRITICAL ERROR (registerUser): Không thể xóa UserData (ID: " + createdUserData.getId() + ") sau khi tạo Account thất bại! Dữ liệu có thể không nhất quán!");
            } else {
                System.out.println("SERVICE ROLLBACK INFO (registerUser): Đã xóa UserData (ID: " + createdUserData.getId() + ") do tạo Account thất bại.");
            }
            // if (conn != null) conn.rollback();
            return false;
        }

        // if (conn != null) conn.commit(); // Kết thúc transaction nếu thành công
        System.out.println("Người dùng '" + username + "' đã được đăng ký thành công với UserData ID: " + createdUserData.getId());
        return true;
        // } catch (SQLException e) {
        //     System.err.println("SERVICE SQL EXCEPTION (registerUser) trong quá trình transaction: " + e.getMessage());
        //     e.printStackTrace();
        //     if (conn != null) try { conn.rollback(); } catch (SQLException se) { System.err.println("Lỗi khi rollback: " + se.getMessage()); }
        //     return false;
        // } finally {
        //     if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException se) { System.err.println("Lỗi khi đóng connection: " + se.getMessage()); }
        // }
        // ----- KẾT THÚC KHỐI LOGIC CẦN CÂN NHẮC TRANSACTION -----
    }
}