package com.kimquyen.healthapp.util;

import com.kimquyen.healthapp.dao.AccountDAO;
import com.kimquyen.healthapp.dao.UserDataDAO;
import com.kimquyen.healthapp.model.Account;
import com.kimquyen.healthapp.model.Role;
import com.kimquyen.healthapp.model.UserData;
import com.kimquyen.healthapp.service.BCryptPasswordHashingServiceImpl; // Import implementation cụ thể
import com.kimquyen.healthapp.service.PasswordHashingService;

import java.util.List;
import java.util.Locale; // Để chuẩn hóa username

public class AccountGeneratorUtil {

    public static void main(String[] args) {
        UserDataDAO userDataDAO = new UserDataDAO();
        AccountDAO accountDAO = new AccountDAO();
        PasswordHashingService passwordHashingService = new BCryptPasswordHashingServiceImpl();

        String defaultPlainPassword = "user123"; 
        String defaultHashedPassword = passwordHashingService.hashPassword(defaultPlainPassword);

        System.out.println("Bắt đầu quá trình tạo Account cho UserData chưa có...");

        List<UserData> allUserData = userDataDAO.getAllUsers();

        if (allUserData == null || allUserData.isEmpty()) {
            System.out.println("Không tìm thấy UserData nào.");
            return;
        }

        int accountsCreated = 0;
        int accountsSkipped = 0;

        for (UserData userData : allUserData) {
            if (userData == null || userData.getId() == 0) {
                System.out.println("Bỏ qua UserData không hợp lệ: " + userData);
                continue;
            }

            // Kiểm tra xem đã có Account nào liên kết với UserData ID này chưa
            Account existingAccountByUserId = accountDAO.getAccountByUserId(userData.getId());
            if (existingAccountByUserId != null) {
                System.out.println("UserData ID " + userData.getId() + " (" + userData.getName() + ") đã có Account liên kết (username: " + existingAccountByUserId.getUsername() + "). Bỏ qua.");
                accountsSkipped++;
                continue;
            }

            String baseUsername = generateUsernameFromName(userData.getName());
            String finalUsername = baseUsername;
            int attempt = 0;
            while (accountDAO.getAccountByUsername(finalUsername) != null) {
                attempt++;
                finalUsername = baseUsername + attempt; 
            }

            System.out.println("Đang xử lý UserData ID: " + userData.getId() + " (" + userData.getName() + ")");
            System.out.println("  -> Tạo username: " + finalUsername);

            Account newAccount = new Account(
                    finalUsername,
                    defaultHashedPassword,
                    Role.USER, // Vai trò mặc định là USER
                    userData.getId() 
            );

            if (accountDAO.addAccount(newAccount)) {
                System.out.println("  -> Đã tạo Account thành công cho username: " + finalUsername);
                accountsCreated++;
            } else {
                System.err.println("  -> LỖI khi tạo Account cho username: " + finalUsername + " (UserData ID: " + userData.getId() + ")");
            }
        }

        System.out.println("\nHoàn tất quá trình.");
        System.out.println("Số Account đã tạo mới: " + accountsCreated);
        System.out.println("Số UserData đã có Account (bỏ qua): " + accountsSkipped);
        System.out.println("Mật khẩu mặc định cho các tài khoản mới được tạo là: " + defaultPlainPassword);
    }

    /**
     * Tạo username cơ bản từ tên đầy đủ.
     * Ví dụ: "John Doe" -> "johndoe"
     *         "Jane O'Malley-Smith" -> "janeomalley-smith"
     * Bạn có thể tùy chỉnh logic này.
     */
    private static String generateUsernameFromName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "user" + System.currentTimeMillis() % 10000; 
        }
        String username = fullName.trim().toLowerCase(Locale.ENGLISH);
        username = username.replaceAll("\\s+", ""); 
        username = username.replaceAll("[^a-z0-9]", "");

        if (username.isEmpty()) {
            return "user" + System.currentTimeMillis() % 10000;
        }
        return username.length() > 20 ? username.substring(0, 20) : username;
    }
}