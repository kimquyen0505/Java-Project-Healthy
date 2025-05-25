package com.kimquyen.healthapp.model;

public enum Role {
    ADMIN,
    USER; 

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

        System.err.println("Cảnh báo Role Enum: Không thể chuyển đổi '" + text + "' thành Role. Trả về null.");
        return null;
    }
}