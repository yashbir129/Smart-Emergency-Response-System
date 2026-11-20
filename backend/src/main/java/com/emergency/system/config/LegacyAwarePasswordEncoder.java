package com.emergency.system.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.regex.Pattern;

public class LegacyAwarePasswordEncoder implements PasswordEncoder {
    private static final Pattern BCRYPT_HASH =
            Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String storedPassword) {
        if (BCRYPT_HASH.matcher(storedPassword).matches()) {
            return bcrypt.matches(rawPassword, storedPassword);
        }
        return storedPassword.contentEquals(rawPassword);
    }

    public boolean isLegacy(String storedPassword) {
        return !BCRYPT_HASH.matcher(storedPassword).matches();
    }
}
