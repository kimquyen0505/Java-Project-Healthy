// package com.yourgroupname.healthapp.service;
package com.kimquyen.healthapp.service; // Sử dụng package của bạn

import com.kimquyen.healthapp.util.ValidationUtil;
import com.kimquyen.healthapp.dao.AccountDAO;
import com.kimquyen.healthapp.dao.UserDataDAO;
import com.kimquyen.healthapp.model.Account;
import com.kimquyen.healthapp.model.Role;
import com.kimquyen.healthapp.model.UserData;

import java.sql.Timestamp;
import java.util.List;

public class UserService {
    private final UserDataDAO userDataDAO;
    private final AccountDAO accountDAO;
    private final PasswordHashingService passwordHashingService;

    public UserService(UserDataDAO userDataDAO, AccountDAO accountDAO, PasswordHashingService passwordHashingService) {
        this.userDataDAO = userDataDAO;
        this.accountDAO = accountDAO;
        this.passwordHashingService = passwordHashingService;
    }

    
    public List<UserData> getAllUserData() {
        return userDataDAO.getAllUsers();
    }
    public Account getAccountForUserData(int userId) {
        if (userId <= 0) {
            System.err.println("SERVICE (getAccountForUserData): User ID không hợp lệ.");
            return null;
        }
        return accountDAO.getAccountByUserId(userId); // Gọi phương thức trong AccountDAO
    }
    
    public UserData getUserDataById(int userId) {
        if (userId <= 0) {
            System.err.println("SERVICE (getUserDataById): User ID không hợp lệ.");
            return null;
        }
        return userDataDAO.getUserById(userId); // Gọi phương thức trong UserDataDAO
    }
    
    public Account getAccountByUsername(String username) {
        if (ValidationUtil.isNullOrEmpty(username)) { // Sử dụng ValidationUtil
            System.err.println("SERVICE (getAccountByUsername): Username không được để trống.");
            return null;
        }
        return accountDAO.getAccountByUsername(username); // Gọi phương thức trong AccountDAO
    }


    /**
     * Tạo người dùng mới (UserData và Account liên kết).
     * Dùng cho Admin tạo tài khoản.
     */
    public boolean createUser(UserData userData, String username, String plainPassword, Role role) {
        if (userData == null || username == null || username.trim().isEmpty() ||
            plainPassword == null || plainPassword.isEmpty() || role == null) {
            System.err.println("Thông tin người dùng, username, mật khẩu, và vai trò không được để trống.");
            return false;
        }

        // 1. Kiểm tra username tồn tại
        if (accountDAO.getAccountByUsername(username) != null) {
            System.err.println("Tên đăng nhập '" + username + "' đã tồn tại.");
            return false;
        }

        // 2. Đảm bảo UserData có các trường cần thiết (nếu chưa được đặt từ bên ngoài)
        if (userData.getCreatedAt() == null) {
            userData.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }
        // Nếu ID của UserData chưa được set (ví dụ = 0), addUser sẽ tạo ID mới
        UserData createdUserData = userDataDAO.addUser(userData);
        if (createdUserData == null || createdUserData.getId() == 0) {
            System.err.println("Không thể tạo UserData.");
            return false;
        }

        // 3. Băm mật khẩu
        String hashedPassword = passwordHashingService.hashPassword(plainPassword);

        // 4. Tạo Account
        Account newAccount = new Account(username, hashedPassword, role,createdUserData.getId());
        boolean accountCreated = accountDAO.addAccount(newAccount);

        if (!accountCreated) {
            System.err.println("Không thể tạo Account. Cân nhắc rollback UserData.");
            // userDataDAO.deleteUser(createdUserData.getId()); // Cần xử lý cẩn thận
            return false;
        }
        System.out.println("Admin đã tạo người dùng: " + username + " với UserData ID: " + createdUserData.getId());
        return true;
    }

    /**
     * Cập nhật thông tin người dùng (UserData và Account liên kết).
     * Account có thể chứa mật khẩu mới (dạng plain text) hoặc vai trò mới.
     * Nếu mật khẩu trong đối tượng Account được truyền vào không rỗng, nó sẽ được băm và cập nhật.
     */
    public boolean updateUser(UserData userData, Account accountChanges) {
        if (userData == null || userData.getId() == 0 || accountChanges == null || accountChanges.getUsername() == null) {
            System.err.println("UserData (với ID) và Account (với username) không được null khi cập nhật.");
            return false;
        }

        // 1. Cập nhật UserData
        boolean userDataUpdated = userDataDAO.updateUser(userData);
        if (!userDataUpdated) {
            System.err.println("Không thể cập nhật UserData cho ID: " + userData.getId());
            // Không nhất thiết phải dừng ở đây nếu chỉ UserData thất bại, tùy logic
        }

        // 2. Lấy Account hiện tại để cập nhật
        Account existingAccount = accountDAO.getAccountByUsername(accountChanges.getUsername());
        if (existingAccount == null) {
            System.err.println("Không tìm thấy Account để cập nhật với username: " + accountChanges.getUsername());
            return false; // Nếu không có account thì không thể cập nhật account
        }

        // Cập nhật mật khẩu nếu có mật khẩu mới được cung cấp (và nó khác với mật khẩu băm hiện tại)
        // Mật khẩu trong accountChanges.getPassword() được coi là plain text nếu nó được gửi để thay đổi
        if (accountChanges.getPassword() != null && !accountChanges.getPassword().isEmpty()) {
            String newHashedPassword = passwordHashingService.hashPassword(accountChanges.getPassword());
            existingAccount.setPassword(newHashedPassword);
        }

        // Cập nhật vai trò nếu có
        if (accountChanges.getRole() != null) {
            existingAccount.setRole(accountChanges.getRole());
        }

        boolean accountUpdated = accountDAO.updateAccount(existingAccount);
        if (!accountUpdated) {
            System.err.println("Không thể cập nhật Account cho username: " + existingAccount.getUsername());
        }

        return userDataUpdated || accountUpdated; // Trả về true nếu ít nhất một phần được cập nhật
    }


    /**
     * Xóa người dùng (cả UserData và Account liên quan).
     * Cần cẩn thận với các ràng buộc khóa ngoại (ví dụ từ hra_responses).
     */
    public boolean deleteUser(int userDataId, String username) {
         if (username == null || username.trim().isEmpty() || userDataId == 0) {
            System.err.println("UserData ID và Username không được để trống khi xóa.");
            return false;
        }
        // Cân nhắc thứ tự xóa để tránh lỗi khóa ngoại.
        // Thường thì xóa Account trước (nếu không có ràng buộc từ Account đến UserData)
        // Hoặc nếu có ràng buộc từ hra_responses -> users_data, bạn cần xử lý các response đó trước.
        // Hoặc DB có ON DELETE CASCADE.

        // Tạm thời giả định xóa Account trước, rồi UserData
        boolean accountDeleted = accountDAO.deleteAccount(username);
        if (!accountDeleted) {
            System.err.println("Không thể xóa Account: " + username + ". Việc xóa UserData có thể không được thực hiện.");
            // Tùy logic, có thể dừng ở đây hoặc vẫn tiếp tục xóa UserData
        }

        boolean userDataDeleted = userDataDAO.deleteUser(userDataId);
        if (!userDataDeleted) {
            System.err.println("Không thể xóa UserData cho ID: " + userDataId);
        }

        if(accountDeleted && userDataDeleted) {
            System.out.println("Đã xóa người dùng: " + username + " và UserData ID: " + userDataId);
            return true;
        } else {
            System.err.println("Có lỗi xảy ra trong quá trình xóa người dùng: " + username);
            return false;
        }
    }
}