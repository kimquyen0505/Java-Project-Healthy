// Trong file Role.java
package com.kimquyen.healthapp.model;

public enum Role {
    ADMIN,
    USER; // Giữ chữ hoa cho hằng số Enum

    // Phương thức để lấy Role từ String, không phân biệt chữ hoa/thường
    public static Role fromString(String text) {
        if (text != null) {
            for (Role r : Role.values()) {
                // So sánh không phân biệt chữ hoa/thường với tên của hằng số Enum
                if (text.equalsIgnoreCase(r.name())) {
                    return r;
                }
            }
        }
        // Nếu không tìm thấy hoặc text là null, trả về null
        // Tầng gọi (DAO) sẽ xử lý trường hợp null này
        System.err.println("Cảnh báo Role Enum: Không thể chuyển đổi '" + text + "' thành Role. Trả về null.");
        return null;
    }
}