package com.webapp.web;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class Validate {

    // Username pattern: only letters, numbers, and underscores
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    // Password pattern: at least 8 characters, at least one uppercase letter, one lowercase letter, and one number
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$");

    // Email pattern: any number of letters, numbers, underscores, hyphens, and periods, followed by an @ symbol, followed by any number of letters, numbers, hyphens, and periods
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$");

    // Name pattern: only letters
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z]+$");

    // Date patter: yyyy-mm-dd
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    // Validation methods
    boolean isValidUsername(String username) {
        return USERNAME_PATTERN.matcher(username).matches();
    }

    boolean isValidPassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    boolean isValidName(String name) {
        return NAME_PATTERN.matcher(name).matches();
    }

    boolean isValidDate(String date) {
        return DATE_PATTERN.matcher(date).matches();
    }
}
