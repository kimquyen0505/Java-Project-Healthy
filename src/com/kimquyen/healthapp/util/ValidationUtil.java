package com.kimquyen.healthapp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtil {

    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
            "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{3,20}$";
    private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);

    private ValidationUtil() {
    }

    /**
     * Kiểm tra xem một chuỗi có rỗng hoặc null không.
     *
     * @param input Chuỗi cần kiểm tra.
     * @return true nếu chuỗi là null hoặc rỗng (sau khi đã trim), false nếu ngược lại.
     */
    public static boolean isNullOrEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    /**
     * Kiểm tra xem một chuỗi có phải là địa chỉ email hợp lệ không.
     *
     * @param email Chuỗi email cần kiểm tra.
     * @return true nếu email hợp lệ, false nếu ngược lại.
     */
    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) {
            return false; 
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }

    /**
     * Kiểm tra xem mật khẩu có đáp ứng các yêu cầu cơ bản không.
     * Ví dụ: độ dài tối thiểu.
     *
     * @param password Mật khẩu cần kiểm tra.
     * @param minLength Độ dài tối thiểu cho phép.
     * @return true nếu mật khẩu hợp lệ, false nếu ngược lại.
     */
    public static boolean isValidPassword(String password, int minLength) {
        if (isNullOrEmpty(password)) {
            return false; 
        }
        return password.length() >= minLength;
    }

    /**
     * Kiểm tra xem mật khẩu có đáp ứng các yêu cầu phức tạp hơn không.
     * Ví dụ: phải chứa chữ hoa, chữ thường, số, ký tự đặc biệt.
     * Bạn có thể mở rộng phương thức này.
     *
     * @param password Mật khẩu cần kiểm tra.
     * @return true nếu mật khẩu đáp ứng yêu cầu, false nếu ngược lại.
     */
    public static boolean isStrongPassword(String password) {
        if (isNullOrEmpty(password) || password.length() < 8) { 
            return false;
        }
        boolean hasUpperCase = !password.equals(password.toLowerCase());
        boolean hasLowerCase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");

        return hasUpperCase && hasLowerCase && hasDigit; 
    }

    /**
     * Kiểm tra xem username có hợp lệ không dựa trên biểu thức chính quy.
     *
     * @param username Username cần kiểm tra.
     * @return true nếu username hợp lệ, false nếu ngược lại.
     */
    public static boolean isValidUsername(String username) {
        if (isNullOrEmpty(username)) {
            return false;
        }
        Matcher matcher = USERNAME_PATTERN.matcher(username);
        return matcher.matches();
    }

    /**
     * Kiểm tra xem một chuỗi có độ dài nằm trong khoảng cho phép không.
     *
     * @param input Chuỗi cần kiểm tra.
     * @param minLength Độ dài tối thiểu (bao gồm).
     * @param maxLength Độ dài tối đa (bao gồm).
     * @return true nếu độ dài hợp lệ, false nếu ngược lại.
     */
    public static boolean isLengthValid(String input, int minLength, int maxLength) {
        if (input == null) { // Chuỗi null không có độ dài hợp lệ trừ khi minLength là 0 và bạn chấp nhận null
            return minLength == 0; // Hoặc trả về false nếu null không được phép
        }
        int length = input.length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Kiểm tra xem một chuỗi có phải là số nguyên hợp lệ không.
     *
     * @param numberString Chuỗi cần kiểm tra.
     * @return true nếu là số nguyên hợp lệ, false nếu ngược lại.
     */
    public static boolean isValidInteger(String numberString) {
        if (isNullOrEmpty(numberString)) {
            return false;
        }
        try {
            Integer.parseInt(numberString);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Kiểm tra xem một chuỗi có phải là số thực (double) hợp lệ không.
     *
     * @param numberString Chuỗi cần kiểm tra.
     * @return true nếu là số thực hợp lệ, false nếu ngược lại.
     */
    public static boolean isValidDouble(String numberString) {
        if (isNullOrEmpty(numberString)) {
            return false;
        }
        try {
            Double.parseDouble(numberString);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}