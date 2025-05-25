package com.kimquyen.healthapp.service;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptPasswordHashingServiceImpl implements PasswordHashingService {

    private static final int BCRYPT_WORKLOAD = 12;

    /**
     * Băm một mật khẩu dạng text sử dụng thuật toán BCrypt.
     * @param plainPassword Mật khẩu dạng text cần băm.
     * @return Chuỗi mật khẩu đã được băm.
     * @throws IllegalArgumentException nếu plainPassword là null hoặc rỗng.
     */
    @Override
    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống khi băm.");
        }

        String salt = BCrypt.gensalt(BCRYPT_WORKLOAD);
        return BCrypt.hashpw(plainPassword, salt);
    }

    /**
     * Kiểm tra một mật khẩu dạng text có khớp với một chuỗi mật khẩu đã được băm (sử dụng BCrypt) không.
     * @param plainPassword Mật khẩu dạng text người dùng nhập.
     * @param hashedPasswordFromDB Chuỗi mật khẩu đã được băm lấy từ cơ sở dữ liệu.
     * @return true nếu mật khẩu khớp, false nếu không khớp hoặc có lỗi.
     */
    @Override
    public boolean checkPassword(String plainPassword, String hashedPasswordFromDB) {
        if (plainPassword == null || hashedPasswordFromDB == null ||
            plainPassword.isEmpty() || hashedPasswordFromDB.isEmpty()) {
            return false;
        }

        boolean passwordMatch = false;
        try {
            passwordMatch = BCrypt.checkpw(plainPassword, hashedPasswordFromDB);
        } catch (IllegalArgumentException e) {
            System.err.println("LỖI KIỂM TRA MẬT KHẨU: Định dạng chuỗi băm không hợp lệ. " +
                               "Mật khẩu từ DB: '" + hashedPasswordFromDB + "'. Lỗi: " + e.getMessage());
            passwordMatch = false;
        }
        return passwordMatch;
    }
}