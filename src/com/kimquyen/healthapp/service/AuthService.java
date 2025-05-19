package com.kimquyen.healthapp.service;

import com.kimquyen.healthapp.dao.AccountDAO;
import com.kimquyen.healthapp.dao.UserDataDAO; // Cần cho registerUser
import com.kimquyen.healthapp.model.Account;
import com.kimquyen.healthapp.model.Role; // Cần cho registerUser
import com.kimquyen.healthapp.model.UserData; // Cần cho registerUser
import java.sql.Timestamp; // Cần cho registerUser

public class AuthService {
 private final AccountDAO accountDAO;
 private final UserDataDAO userDataDAO; // Thêm UserDataDAO
 private final PasswordHashingService passwordHashingService;

 // Constructor để inject dependencies
 public AuthService(AccountDAO accountDAO, UserDataDAO userDataDAO, PasswordHashingService passwordHashingService) {
     this.accountDAO = accountDAO;
     this.userDataDAO = userDataDAO;
     this.passwordHashingService = passwordHashingService;
 }

 public Account login(String username, String plainPassword) {
     if (username == null || username.trim().isEmpty() || plainPassword == null || plainPassword.isEmpty()) {
         System.err.println("Tên đăng nhập hoặc mật khẩu không được để trống.");
         return null;
     }

     Account account = accountDAO.getAccountByUsername(username);
     if (account != null) {
         // Mật khẩu lấy từ DAO là mật khẩu đã băm
         if (passwordHashingService.checkPassword(plainPassword, account.getPassword())) {
             return account; // Đăng nhập thành công
         } else {
             System.err.println("Sai mật khẩu cho người dùng: " + username);
         }
     } else {
         System.err.println("Không tìm thấy người dùng: " + username);
     }
     return null; // Đăng nhập thất bại
 }

 /**
  * Đăng ký người dùng mới.
  * Tạo UserData trước, sau đó tạo Account liên kết.
  * Mật khẩu sẽ được băm trước khi lưu.
  * Vai trò mặc định cho người dùng mới là USER.
  */
 public boolean registerUser(String username, String plainPassword, String name, Integer sponsorId) {
     if (username == null || username.trim().isEmpty() ||
         plainPassword == null || plainPassword.isEmpty() ||
         name == null || name.trim().isEmpty()) {
         System.err.println("Tên đăng nhập, mật khẩu, và tên không được để trống khi đăng ký.");
         return false;
     }

     // 1. Kiểm tra xem username đã tồn tại chưa
     if (accountDAO.getAccountByUsername(username) != null) {
         System.err.println("Tên đăng nhập '" + username + "' đã tồn tại.");
         return false;
     }

     // 2. Tạo UserData
     UserData newUserInfo = new UserData();
     newUserInfo.setName(name);
     if (sponsorId != null) { // Cho phép sponsorId là null
          newUserInfo.setSponsorId(sponsorId);
     }
     newUserInfo.setCreatedAt(new Timestamp(System.currentTimeMillis()));

     UserData createdUserData = userDataDAO.addUser(newUserInfo); // Giả sử addUser trả về UserData với ID
     if (createdUserData == null || createdUserData.getId() == 0) {
         System.err.println("Không thể tạo thông tin người dùng (UserData).");
         return false;
     }

     // 3. Băm mật khẩu
     String hashedPassword = passwordHashingService.hashPassword(plainPassword);

     // 4. Tạo Account
     // Vai trò mặc định cho người dùng mới khi đăng ký thường là USER
     // ID của UserData không trực tiếp lưu trong bảng account,
     // username là khóa chính hoặc duy nhất trong bảng account.
     Account newAccount = new Account(username, hashedPassword, Role.USER,createdUserData.getId());

     boolean accountCreated = accountDAO.addAccount(newAccount);
     if (!accountCreated) {
         System.err.println("Không thể tạo tài khoản (Account). Cân nhắc rollback UserData đã tạo.");
         // Trong ứng dụng thực tế, bạn cần cơ chế rollback UserData nếu tạo Account thất bại.
         // Ví dụ: userDataDAO.deleteUser(createdUserData.getId()); (Cẩn thận với ràng buộc khóa ngoại)
         return false;
     }

     System.out.println("Người dùng '" + username + "' đã được đăng ký thành công với UserData ID: " + createdUserData.getId());
     return true;
 }
}
