package com.kimquyen.healthapp.service;

public interface PasswordHashingService {
 String hashPassword(String plainPassword);
 boolean checkPassword(String plainPassword, String hashedPassword);
}

