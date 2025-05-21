// Ví dụ: Tạo file PasswordHasherUtil.java
package com.kimquyen.healthapp.util; // Hoặc một package tiện ích khác

import com.kimquyen.healthapp.service.BCryptPasswordHashingServiceImpl;
import com.kimquyen.healthapp.service.PasswordHashingService;

public class PasswordHasherUtil {
    public static void main(String[] args) {
        // Sử dụng đúng implementation mà ứng dụng của bạn đang dùng
        PasswordHashingService hasher = new BCryptPasswordHashingServiceImpl();

        String plainAdminPass = "admin123";
        String hashedAdminPass = hasher.hashPassword(plainAdminPass);
        System.out.println("Username: admin");
        System.out.println("Plain Password: " + plainAdminPass);
        System.out.println("Hashed Password: " + hashedAdminPass); // << COPY GIÁ TRỊ NÀY

        System.out.println("\n----------------------------------\n");

        String plainUser1Pass = "user123";
        String hashedUser1Pass = hasher.hashPassword(plainUser1Pass);
        System.out.println("Username: user1");
        System.out.println("Plain Password: " + plainUser1Pass);
        System.out.println("Hashed Password: " + hashedUser1Pass); // << COPY GIÁ TRỊ NÀY
        
       
        String plainSuperAdminPass = "admin123"; // Mật khẩu bạn muốn cho superadmin
        String hashedSuperAdminPass = hasher.hashPassword(plainSuperAdminPass);
        System.out.println("Username: superadmin");
        System.out.println("Plain Password: " + plainSuperAdminPass);
        System.out.println("Hashed Password FOR SUPERADMIN: " + hashedSuperAdminPass); // << COPY GIÁ TRỊ NÀY
        
    }
}