package com.kimquyen.healthapp.service; // Đảm bảo package này đúng

import org.mindrot.jbcrypt.BCrypt; // Import thư viện jBCrypt

// Lớp này triển khai interface PasswordHashingService
public class BCryptPasswordHashingServiceImpl implements PasswordHashingService {

    // Độ mạnh của việc băm (số vòng lặp). Giá trị khuyến nghị là 10-12.
    // Càng cao càng an toàn nhưng càng tốn thời gian xử lý.
    private static final int BCRYPT_WORKLOAD = 12;

    /**
     * Băm một mật khẩu dạng text sử dụng thuật toán BCrypt.
     *
     * @param plainPassword Mật khẩu dạng text cần băm.
     * @return Chuỗi mật khẩu đã được băm.
     * @throws IllegalArgumentException nếu plainPassword là null hoặc rỗng.
     */
    @Override
    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống khi băm.");
        }
        // BCrypt.gensalt() sẽ tạo ra một "salt" ngẫu nhiên
        // và kết hợp nó vào chuỗi băm kết quả.
        String salt = BCrypt.gensalt(BCRYPT_WORKLOAD);
        return BCrypt.hashpw(plainPassword, salt);
    }

    /**
     * Kiểm tra một mật khẩu dạng text có khớp với một chuỗi mật khẩu đã được băm (sử dụng BCrypt) không.
     *
     * @param plainPassword Mật khẩu dạng text người dùng nhập.
     * @param hashedPasswordFromDB Chuỗi mật khẩu đã được băm lấy từ cơ sở dữ liệu.
     * @return true nếu mật khẩu khớp, false nếu không khớp hoặc có lỗi.
     */
    @Override
    public boolean checkPassword(String plainPassword, String hashedPasswordFromDB) {
        if (plainPassword == null || hashedPasswordFromDB == null ||
            plainPassword.isEmpty() || hashedPasswordFromDB.isEmpty()) {
            // Không thể so sánh nếu một trong hai là rỗng hoặc null
            return false;
        }

        boolean passwordMatch = false;
        try {
            // BCrypt.checkpw sẽ tự động trích xuất salt từ hashedPasswordFromDB
            // để so sánh với plainPassword.
            passwordMatch = BCrypt.checkpw(plainPassword, hashedPasswordFromDB);
        } catch (IllegalArgumentException e) {
            // Điều này có thể xảy ra nếu hashedPasswordFromDB không phải là một chuỗi băm BCrypt hợp lệ.
            // (Ví dụ: quá ngắn, thiếu tiền tố $2a$, $2b$, $2y$)
            System.err.println("LỖI KIỂM TRA MẬT KHẨU: Định dạng chuỗi băm không hợp lệ. " +
                               "Mật khẩu từ DB: '" + hashedPasswordFromDB + "'. Lỗi: " + e.getMessage());
            // Trong trường hợp này, coi như mật khẩu không khớp.
            passwordMatch = false;
        }
        return passwordMatch;
    }
}