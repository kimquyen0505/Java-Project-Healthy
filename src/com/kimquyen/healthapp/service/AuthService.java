package com.kimquyen.healthapp.service;

import com.kimquyen.healthapp.util.ValidationUtil; 
import com.kimquyen.healthapp.dao.AccountDAO;
import com.kimquyen.healthapp.dao.UserDataDAO;
import com.kimquyen.healthapp.model.Account;
import com.kimquyen.healthapp.model.Role;
import com.kimquyen.healthapp.model.UserData;

import java.sql.Timestamp;


public class AuthService {
    private final AccountDAO accountDAO;
    private final UserDataDAO userDataDAO;
    private final PasswordHashingService passwordHashingService;

    public AuthService(AccountDAO accountDAO, UserDataDAO userDataDAO, PasswordHashingService passwordHashingService) {
        if (accountDAO == null || userDataDAO == null || passwordHashingService == null) {
            throw new IllegalArgumentException("Các DAO và PasswordHashingService không được null khi khởi tạo AuthService.");
        }
        this.accountDAO = accountDAO;
        this.userDataDAO = userDataDAO;
        this.passwordHashingService = passwordHashingService;
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
        if (ValidationUtil.isNullOrEmpty(username) ||
            ValidationUtil.isNullOrEmpty(plainPassword) ||
            ValidationUtil.isNullOrEmpty(name)) {
            System.err.println("SERVICE (registerUser): Tên đăng nhập, mật khẩu, và tên không được để trống.");
            return false;
        }
        if (!ValidationUtil.isValidUsername(username)) { 
             System.err.println("SERVICE (registerUser): Định dạng tên đăng nhập không hợp lệ (ví dụ: 3-20 ký tự, chỉ chữ cái, số, dấu gạch dưới).");
             return false;
        }
        if (!ValidationUtil.isValidPassword(plainPassword, 8)) { 
             System.err.println("SERVICE (registerUser): Mật khẩu phải có ít nhất 8 ký tự.");
             return false;
        }
     
        //  Kiểm tra xem username đã tồn tại chưa
        if (accountDAO.getAccountByUsername(username) != null) {
            System.err.println("SERVICE (registerUser): Tên đăng nhập '" + username + "' đã tồn tại.");
            return false;
        }

        //  Tạo UserData
        UserData newUserInfo = new UserData();
        newUserInfo.setName(name);
        if (sponsorId != null && sponsorId != 0) { 
             newUserInfo.setSponsorId(sponsorId);
        } else {
            newUserInfo.setSponsorId(0); 
        }
        newUserInfo.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        UserData createdUserData = userDataDAO.addUser(newUserInfo); 
        if (createdUserData == null || createdUserData.getId() == 0) {
            System.err.println("SERVICE ERROR (registerUser): Không thể tạo UserData. Kiểm tra log của UserDataDAO (có thể lỗi SQL, ràng buộc DB).");
            return false;
        }
        System.out.println("SERVICE INFO (registerUser): UserData đã tạo với ID: " + createdUserData.getId() + " cho tên: " + createdUserData.getName());

        //  Băm mật khẩu
        String hashedPassword = passwordHashingService.hashPassword(plainPassword);

        //  Tạo Account
        Account newAccount = new Account(username, hashedPassword, Role.USER, createdUserData.getId());

        System.out.println("SERVICE INFO (registerUser): Đang tạo Account cho username: '" + username + "' với UserData ID: " + newAccount.getUserId());
        boolean accountCreated = accountDAO.addAccount(newAccount); 

        if (!accountCreated) {
            System.err.println("SERVICE ERROR (registerUser): Không thể tạo Account. Kiểm tra log của AccountDAO. Đang cố gắng rollback UserData...");

            boolean userDeleted = userDataDAO.deleteUser(createdUserData.getId()); 
            if (!userDeleted) {
                System.err.println("SERVICE ROLLBACK CRITICAL ERROR (registerUser): Không thể xóa UserData (ID: " + createdUserData.getId() + ") sau khi tạo Account thất bại! Dữ liệu có thể không nhất quán!");
            } else {
                System.out.println("SERVICE ROLLBACK INFO (registerUser): Đã xóa UserData (ID: " + createdUserData.getId() + ") do tạo Account thất bại.");
            }
            return false;
        }

        System.out.println("Người dùng '" + username + "' đã được đăng ký thành công với UserData ID: " + createdUserData.getId());
        return true;
        
    }
}