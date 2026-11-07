package com.autobeauty.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "123456";
        String hashedPassword = encoder.encode(password);
        
        System.out.println("Original password: " + password);
        System.out.println("Hashed password: " + hashedPassword);
        
        boolean matches = encoder.matches(password, hashedPassword);
        System.out.println("Password verification: " + matches);
        
        String dbHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8imdqMNRfFEZkxRNlALLFPXjKcJJe";
        boolean dbMatches = encoder.matches(password, dbHash);
        System.out.println("DB password verification: " + dbMatches);
        
        String[] testPasswords = {"admin", "password", "123456789", "admin123"};
        for (String testPwd : testPasswords) {
            boolean testMatches = encoder.matches(testPwd, dbHash);
            System.out.println("Test password '" + testPwd + "': " + testMatches);
        }
    }
}
