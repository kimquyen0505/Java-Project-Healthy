package com.kimquyen.healthapp.service;

//package com.yourgroupname.healthapp.service;

public interface PasswordHashingService {
 String hashPassword(String plainPassword);
 boolean checkPassword(String plainPassword, String hashedPassword);
}

//Ví dụ một lớp triển khai (bạn cần hoàn thiện bằng thư viện thực tế)
//class BCryptPasswordHashingService implements PasswordHashingService {
//  @Override
//  public String hashPassword(String plainPassword) {
//      // Sử dụng thư viện BCrypt để băm, ví dụ:
//      // return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
//      throw new UnsupportedOperationException("Chưa triển khai hashPassword với thư viện thực tế");
//  }

//  @Override
//  public boolean checkPassword(String plainPassword, String hashedPassword) {
//      // Sử dụng thư viện BCrypt để kiểm tra, ví dụ:
//      // try {
//      //     return BCrypt.checkpw(plainPassword, hashedPassword);
//      // } catch (IllegalArgumentException e) {
//      //     // Xử lý trường hợp hashedPassword không phải là định dạng bcrypt hợp lệ
//      //     return false;
//      // }
//      throw new UnsupportedOperationException("Chưa triển khai checkPassword với thư viện thực tế");
//  }
//}
