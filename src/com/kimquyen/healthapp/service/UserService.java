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

    public UserService(UserDataDAO userDataDAO, AccountDAO accountDAO, PasswordHashingService passwordHashingService,SponsorDAO sponsorDAO) {
        if (userDataDAO == null || accountDAO == null || passwordHashingService == null || sponsorDAO == null) {
            throw new IllegalArgumentException("DAOs và PasswordHashingService không được null khi khởi tạo UserService.");
        }
        this.userDataDAO = userDataDAO;
        this.accountDAO = accountDAO;
        this.passwordHashingService = passwordHashingService;
        this.sponsorDAO = sponsorDAO;
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
        if (userId <= 0) { return null; }
        UserData result = userDataDAO.getUserById(userId); 
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

            if (sponsorId == null || sponsorId == 0) { 
            	sponsorName = "Không có nhà tài trợ";
            } else {
                Sponsor sponsor = sponsorDAO.getSponsorById(sponsorId);
                sponsorName = (sponsor != null && sponsor.getName() != null && !sponsor.getName().trim().isEmpty())
                                ? sponsor.getName()
                                : "Nhà tài trợ ID: " + sponsorId; 
            }

            distribution.put(sponsorName, distribution.getOrDefault(sponsorName, 0L) + count);
        }
        return distribution;
    }

    public boolean createUser(UserData userData, String username, String plainPassword, Role role) {
        if (userData == null || ValidationUtil.isNullOrEmpty(username) ||
            ValidationUtil.isNullOrEmpty(plainPassword) || role == null ||
            ValidationUtil.isNullOrEmpty(userData.getName())) { 
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

        UserData createdUserData = userDataDAO.addUser(userData);
        if (createdUserData == null || createdUserData.getId() == 0) {
            System.err.println("SERVICE ERROR (createUser): Không thể tạo UserData. Kiểm tra log của UserDataDAO.");
            return false;
        }
        System.out.println("SERVICE INFO (createUser): UserData đã tạo với ID: " + createdUserData.getId());

        String hashedPassword = passwordHashingService.hashPassword(plainPassword);
        Account newAccount = new Account(username, hashedPassword, role, createdUserData.getId());

        System.out.println("SERVICE INFO (createUser): Đang tạo Account cho username: " + username + " với UserData ID: " + newAccount.getUserId());
        boolean accountCreated = accountDAO.addAccount(newAccount); 

        if (!accountCreated) {
            System.err.println("SERVICE ERROR (createUser): Không thể tạo Account. Kiểm tra log của AccountDAO. Đang cố gắng rollback UserData...");
            boolean userDeleted = userDataDAO.deleteUser(createdUserData.getId()); // userDataDAO.deleteUser(createdUserData.getId(), conn);
            if (!userDeleted) {
                System.err.println("SERVICE ROLLBACK ERROR (createUser): Không thể xóa UserData (ID: " + createdUserData.getId() + ") sau khi tạo Account thất bại. Dữ liệu có thể không nhất quán!");
            } else {
                System.out.println("SERVICE ROLLBACK INFO (createUser): Đã xóa UserData (ID: " + createdUserData.getId() + ") do tạo Account thất bại.");
            }
            return false;
        }

        System.out.println("Admin đã tạo người dùng: " + username + " với UserData ID: " + createdUserData.getId());
        return true;

    }

    public boolean updateUser(UserData userData, Account accountChanges) {
        if (userData == null || userData.getId() == 0 || accountChanges == null || ValidationUtil.isNullOrEmpty(accountChanges.getUsername())) {
            System.err.println("SERVICE (updateUser): UserData (với ID) và Account (với username) không được null/rỗng khi cập nhật.");
            return false;
        }

        boolean overallSuccess = true; 

        //  Cập nhật UserData
        if (!userDataDAO.updateUser(userData)) {
            System.err.println("SERVICE WARNING (updateUser): Không thể cập nhật UserData cho ID: " + userData.getId());
            overallSuccess = false; // Đánh dấu có lỗi nhưng vẫn có thể tiếp tục cập nhật Account
        }

        // Lấy Account hiện tại để cập nhật
        Account existingAccount = accountDAO.getAccountByUsername(accountChanges.getUsername());
        if (existingAccount == null) {
            System.err.println("SERVICE ERROR (updateUser): Không tìm thấy Account để cập nhật với username: " + accountChanges.getUsername());
            return false; 
        }

        if (accountChanges.getPassword() != null && !accountChanges.getPassword().isEmpty()) {
            String newHashedPassword = passwordHashingService.hashPassword(accountChanges.getPassword());
            existingAccount.setPassword(newHashedPassword);
        }


        // Cập nhật vai trò nếu có
        if (accountChanges.getRole() != null) {
            existingAccount.setRole(accountChanges.getRole());
        }

        if (!accountDAO.updateAccount(existingAccount)) {
            System.err.println("SERVICE WARNING (updateUser): Không thể cập nhật Account cho username: " + existingAccount.getUsername());
            overallSuccess = false;
        }

        return overallSuccess; 
    }

    public boolean deleteUser(int userDataId, String username) {
        if (ValidationUtil.isNullOrEmpty(username) || userDataId <= 0) {
            System.err.println("SERVICE (deleteUser): UserData ID hoặc Username không hợp lệ để xóa.");
            return false;
        }

        Account accountToDelete = accountDAO.getAccountByUsername(username);
        if (accountToDelete == null) {
            System.err.println("SERVICE INFO (deleteUser): Không tìm thấy Account với username: " + username + ". Chỉ thử xóa UserData (nếu tồn tại).");
            boolean userDataOnlyDeleted = userDataDAO.deleteUser(userDataId);
            if (userDataOnlyDeleted) {
                System.out.println("SERVICE INFO (deleteUser): Đã xóa UserData ID: " + userDataId + " (không có Account liên kết).");
            } else {
                System.err.println("SERVICE INFO (deleteUser): Không tìm thấy UserData ID: " + userDataId + " để xóa.");
            }
            return userDataOnlyDeleted; 
        }

        if (accountToDelete.getUserId() != userDataId) {
            System.err.println("SERVICE ERROR (deleteUser): Account '" + username + "' (liên kết với UserID: " + accountToDelete.getUserId() + ") " +
                               "không khớp với UserData ID được yêu cầu xóa (" + userDataId + "). Hành động bị hủy.");
            return false;
        }

        boolean accountDeleted = accountDAO.deleteAccount(username); 
        if (!accountDeleted) {
            System.err.println("SERVICE ERROR (deleteUser): Không thể xóa Account: " + username + ". Rollback nếu có.");
            return false;
        }
        System.out.println("SERVICE INFO (deleteUser): Đã xóa Account: " + username);

        // Xóa UserData
        boolean userDataDeleted = userDataDAO.deleteUser(userDataId); 
        if (!userDataDeleted) {
            System.err.println("SERVICE ERROR (deleteUser): Đã xóa Account '" + username + "' nhưng không thể xóa UserData ID: " + userDataId + ". DỮ LIỆU KHÔNG NHẤT QUÁN! Cần rollback.");

            return false; 
        }
        System.out.println("SERVICE INFO (deleteUser): Đã xóa UserData ID: " + userDataId);
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