// package com.kimquyen.healthapp.service;
package com.kimquyen.healthapp.service;

import com.kimquyen.healthapp.util.*;

import com.kimquyen.healthapp.dao.*; // Cần SponsorDAO
import com.kimquyen.healthapp.model.*;   // Cần Sponsor model
import com.kimquyen.healthapp.util.DatabaseUtil;                     


import java.util.Collections; 
import java.sql.Connection; // Cho ví dụ transaction
import java.sql.SQLException; // Cho ví dụ transaction
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class UserService {
    private final UserDataDAO userDataDAO;
    private final AccountDAO accountDAO;
    private final PasswordHashingService passwordHashingService;
    private final SponsorDAO sponsorDAO;
    // private final HraResponseDAO hraResponseDAO; // Thêm nếu cần xóa HraResponse khi xóa User

    public UserService(UserDataDAO userDataDAO, AccountDAO accountDAO, PasswordHashingService passwordHashingService,SponsorDAO sponsorDAO) {
        if (userDataDAO == null || accountDAO == null || passwordHashingService == null || sponsorDAO == null) {
            throw new IllegalArgumentException("DAOs và PasswordHashingService không được null khi khởi tạo UserService.");
        }
        this.userDataDAO = userDataDAO;
        this.accountDAO = accountDAO;
        this.passwordHashingService = passwordHashingService;
        this.sponsorDAO = sponsorDAO;
        // this.hraResponseDAO = hraResponseDAO;
    }

    public List<UserData> getAllUserData() {
        return userDataDAO.getAllUsers();
    }

    public Account getAccountForUserData(int userId) {
        if (userId <= 0) {
            System.err.println("SERVICE (getAccountForUserData): User ID không hợp lệ.");
            return null;
        }
        return accountDAO.getAccountByUserId(userId);
    }

    public UserData getUserDataById(int userId) {
        System.out.println("SERVICE DEBUG: getUserDataById - Nhận userId: " + userId);
        if (userId <= 0) { /* ... */ return null; }
        UserData result = userDataDAO.getUserById(userId); // Giả sử userDataDAO đã được inject đúng
        System.out.println("SERVICE DEBUG: getUserDataById - UserDataDAO.getUserById(" + userId + ") trả về: " + (result != null ? result.getName() : "null"));
        return result;
    }

    public Account getAccountByUsername(String username) {
        if (ValidationUtil.isNullOrEmpty(username)) {
            System.err.println("SERVICE (getAccountByUsername): Username không được để trống.");
            return null;
        }
        return accountDAO.getAccountByUsername(username);
    }
    
    public Map<String, Long> getUserDistributionBySponsor() {
        if (userDataDAO == null || sponsorDAO == null) {
            System.err.println("SERVICE (getUserDistributionBySponsor): userDataDAO hoặc sponsorDAO là null!");
            return Collections.emptyMap();
        }

        Map<Integer, Long> countsBySponsorId = userDataDAO.countUsersBySponsorId();
        Map<String, Long> distribution = new HashMap<>();

        for (Map.Entry<Integer, Long> entry : countsBySponsorId.entrySet()) {
            Integer sponsorId = entry.getKey();
            Long count = entry.getValue();
            String sponsorName;

            if (sponsorId == null || sponsorId == 0) { // Giả sử 0 hoặc NULL là "Không có nhà tài trợ"
                sponsorName = "Không có nhà tài trợ";
            } else {
                Sponsor sponsor = sponsorDAO.getSponsorById(sponsorId);
                sponsorName = (sponsor != null && sponsor.getName() != null && !sponsor.getName().trim().isEmpty())
                                ? sponsor.getName()
                                : "Nhà tài trợ ID: " + sponsorId; // Fallback nếu tên rỗng hoặc không tìm thấy
            }
            // Gộp các nhà tài trợ có cùng tên (nếu có thể xảy ra và bạn muốn vậy)
            // Hoặc đảm bảo tên nhà tài trợ là duy nhất
            distribution.put(sponsorName, distribution.getOrDefault(sponsorName, 0L) + count);
        }
        return distribution;
    }

    public boolean createUser(UserData userData, String username, String plainPassword, Role role) {
        if (userData == null || ValidationUtil.isNullOrEmpty(username) ||
            ValidationUtil.isNullOrEmpty(plainPassword) || role == null ||
            ValidationUtil.isNullOrEmpty(userData.getName())) { // Kiểm tra cả userData.name
            System.err.println("SERVICE (createUser): Thông tin người dùng, username, mật khẩu, tên, và vai trò không được để trống.");
            return false;
        }

        if (accountDAO.getAccountByUsername(username) != null) {
            System.err.println("SERVICE (createUser): Tên đăng nhập '" + username + "' đã tồn tại.");
            return false;
        }

        if (userData.getCreatedAt() == null) {
            userData.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }



        UserData createdUserData = userDataDAO.addUser(userData); // userDataDAO.addUser(userData, conn);
        if (createdUserData == null || createdUserData.getId() == 0) {
            System.err.println("SERVICE ERROR (createUser): Không thể tạo UserData. Kiểm tra log của UserDataDAO.");
            // if (conn != null) conn.rollback();
            return false;
        }
        System.out.println("SERVICE INFO (createUser): UserData đã tạo với ID: " + createdUserData.getId());

        String hashedPassword = passwordHashingService.hashPassword(plainPassword);
        Account newAccount = new Account(username, hashedPassword, role, createdUserData.getId());

        System.out.println("SERVICE INFO (createUser): Đang tạo Account cho username: " + username + " với UserData ID: " + newAccount.getUserId());
        boolean accountCreated = accountDAO.addAccount(newAccount); // accountDAO.addAccount(newAccount, conn);

        if (!accountCreated) {
            System.err.println("SERVICE ERROR (createUser): Không thể tạo Account. Kiểm tra log của AccountDAO. Đang cố gắng rollback UserData...");
            // Cố gắng rollback UserData (xóa UserData vừa tạo)
            boolean userDeleted = userDataDAO.deleteUser(createdUserData.getId()); // userDataDAO.deleteUser(createdUserData.getId(), conn);
            if (!userDeleted) {
                System.err.println("SERVICE ROLLBACK ERROR (createUser): Không thể xóa UserData (ID: " + createdUserData.getId() + ") sau khi tạo Account thất bại. Dữ liệu có thể không nhất quán!");
            } else {
                System.out.println("SERVICE ROLLBACK INFO (createUser): Đã xóa UserData (ID: " + createdUserData.getId() + ") do tạo Account thất bại.");
            }
            // if (conn != null) conn.rollback();
            return false;
        }

        // if (conn != null) conn.commit();
        System.out.println("Admin đã tạo người dùng: " + username + " với UserData ID: " + createdUserData.getId());
        return true;

    }

    public boolean updateUser(UserData userData, Account accountChanges) {
        // ... (Logic updateUser của bạn có vẻ đã khá tốt, chỉ cần đảm bảo các DAO hoạt động) ...
        // Cân nhắc transaction nếu việc cập nhật UserData và Account phải đồng thời thành công.
        if (userData == null || userData.getId() == 0 || accountChanges == null || ValidationUtil.isNullOrEmpty(accountChanges.getUsername())) {
            System.err.println("SERVICE (updateUser): UserData (với ID) và Account (với username) không được null/rỗng khi cập nhật.");
            return false;
        }

        boolean overallSuccess = true; // Theo dõi thành công tổng thể

        // 1. Cập nhật UserData
        if (!userDataDAO.updateUser(userData)) {
            System.err.println("SERVICE WARNING (updateUser): Không thể cập nhật UserData cho ID: " + userData.getId());
            overallSuccess = false; // Đánh dấu có lỗi nhưng vẫn có thể tiếp tục cập nhật Account
        }

        // 2. Lấy Account hiện tại để cập nhật
        Account existingAccount = accountDAO.getAccountByUsername(accountChanges.getUsername());
        if (existingAccount == null) {
            System.err.println("SERVICE ERROR (updateUser): Không tìm thấy Account để cập nhật với username: " + accountChanges.getUsername());
            return false; // Không thể cập nhật Account nếu không tìm thấy
        }

        // Cập nhật mật khẩu nếu có mật khẩu mới được cung cấp
        if (accountChanges.getPassword() != null && !accountChanges.getPassword().isEmpty()) {
            // Service chịu trách nhiệm băm mật khẩu mới trước khi truyền cho DAO
            String newHashedPassword = passwordHashingService.hashPassword(accountChanges.getPassword());
            existingAccount.setPassword(newHashedPassword);
        }
        // Nếu accountChanges.getPassword() là null, nghĩa là không muốn đổi mật khẩu,
        // thì existingAccount.getPassword() (mật khẩu băm cũ) sẽ được giữ nguyên.

        // Cập nhật vai trò nếu có
        if (accountChanges.getRole() != null) {
            existingAccount.setRole(accountChanges.getRole());
        }
        // userId của existingAccount không nên thay đổi ở đây trừ khi có logic đặc biệt

        if (!accountDAO.updateAccount(existingAccount)) {
            System.err.println("SERVICE WARNING (updateUser): Không thể cập nhật Account cho username: " + existingAccount.getUsername());
            overallSuccess = false;
        }

        return overallSuccess; // Trả về true nếu ít nhất một phần (UserData hoặc Account) được cập nhật thành công,
                               // hoặc bạn có thể muốn trả về false nếu bất kỳ phần nào thất bại (cần transaction chặt chẽ hơn).
    }

    public boolean deleteUser(int userDataId, String username) {
        if (ValidationUtil.isNullOrEmpty(username) || userDataId <= 0) {
            System.err.println("SERVICE (deleteUser): UserData ID hoặc Username không hợp lệ để xóa.");
            return false;
        }

        Account accountToDelete = accountDAO.getAccountByUsername(username);
        if (accountToDelete == null) {
            System.err.println("SERVICE INFO (deleteUser): Không tìm thấy Account với username: " + username + ". Chỉ thử xóa UserData (nếu tồn tại).");
            // Chỉ xóa UserData nếu Account không tồn tại (có thể do dữ liệu không nhất quán)
            boolean userDataOnlyDeleted = userDataDAO.deleteUser(userDataId);
            if (userDataOnlyDeleted) {
                System.out.println("SERVICE INFO (deleteUser): Đã xóa UserData ID: " + userDataId + " (không có Account liên kết).");
            } else {
                System.err.println("SERVICE INFO (deleteUser): Không tìm thấy UserData ID: " + userDataId + " để xóa.");
            }
            return userDataOnlyDeleted; // Hoặc false nếu bạn yêu cầu cả hai phải tồn tại để xóa
        }

        // Kiểm tra xem Account có thực sự liên kết với UserData ID này không
        if (accountToDelete.getUserId() != userDataId) {
            System.err.println("SERVICE ERROR (deleteUser): Account '" + username + "' (liên kết với UserID: " + accountToDelete.getUserId() + ") " +
                               "không khớp với UserData ID được yêu cầu xóa (" + userDataId + "). Hành động bị hủy.");
            return false;
        }

        // ----- BẮT ĐẦU KHỐI CẦN TRANSACTION -----
        // Connection conn = null;
        // try {
        //     conn = DatabaseUtil.getConnection();
        //     conn.setAutoCommit(false);

        // Bước 1 (Tùy chọn): Xóa các dữ liệu phụ thuộc trước (ví dụ: hra_responses) nếu không có ON DELETE CASCADE
        // boolean responsesDeleted = hraResponseDAO.deleteResponsesByUserId(userDataId, conn);
        // if (!responsesDeleted) { /* Xử lý lỗi, rollback */ }


        // Bước 2: Xóa Account trước (vì nó có thể có FK đến UserData)
        boolean accountDeleted = accountDAO.deleteAccount(username); // accountDAO.deleteAccount(username, conn);
        if (!accountDeleted) {
            System.err.println("SERVICE ERROR (deleteUser): Không thể xóa Account: " + username + ". Rollback nếu có.");
            // if (conn != null) conn.rollback();
            return false;
        }
        System.out.println("SERVICE INFO (deleteUser): Đã xóa Account: " + username);

        // Bước 3: Xóa UserData
        boolean userDataDeleted = userDataDAO.deleteUser(userDataId); // userDataDAO.deleteUser(userDataId, conn);
        if (!userDataDeleted) {
            System.err.println("SERVICE ERROR (deleteUser): Đã xóa Account '" + username + "' nhưng không thể xóa UserData ID: " + userDataId + ". DỮ LIỆU KHÔNG NHẤT QUÁN! Cần rollback.");
            // Trong thực tế, nếu Account đã bị xóa, bạn không thể "rollback" nó dễ dàng nếu không có transaction.
            // Đây là lý do transaction quan trọng.
            // if (conn != null) conn.rollback();
            return false; // Hoặc ném một exception nghiêm trọng
        }
        System.out.println("SERVICE INFO (deleteUser): Đã xóa UserData ID: " + userDataId);

        // if (conn != null) conn.commit();
        System.out.println("Đã xóa thành công người dùng: " + username + " và UserData ID: " + userDataId);
        return true;
        
    }
    
    public Map<String, Long> getNewUserCountByMonth() {
        if (userDataDAO == null) {
            System.err.println("SERVICE (getNewUserCountByMonth): userDataDAO is null!");
            return Collections.emptyMap();
        }
        return userDataDAO.countNewUsersByMonth();
    }
}