package com.customer.api.service;

import com.customer.api.model.User;
import com.customer.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

// TODO(security): Consider upgrading to JWT for production use (stateless, scalable).
// TODO(security): Consider implementing rate limiting on login attempts.
// TODO(security): Consider MFA to strengthen account authentication.
// TODO(security): Consider leaked password detection (e.g. HaveIBeenPwned API).
// TODO(security): Consider using OAuth providers for authentication.
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ConcurrentHashMap<String, String> activeTokens;
    private final SecureRandom secureRandom;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(12);
        this.activeTokens = new ConcurrentHashMap<>();
        this.secureRandom = new SecureRandom();
    }

    public User register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(username, hashedPassword);
        user.setCreatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        logger.info("New user registered: {}", username);
        return saved;
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        activeTokens.put(token, username);
        logger.info("User logged in: {}", username);
        return token;
    }

    public boolean validateToken(String token) {
        return token != null && activeTokens.containsKey(token);
    }

    public void logout(String token) {
        String username = activeTokens.remove(token);
        if (username != null) {
            logger.info("User logged out: {}", username);
        }
    }

    public void logoutAllSessions(String username) {
        activeTokens.entrySet().removeIf(entry -> entry.getValue().equals(username));
        logger.info("All sessions invalidated for user: {}", username);
    }
}
