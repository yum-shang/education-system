package com.example.education.system.auth.service;

import com.example.education.system.auth.model.User;
import com.example.education.system.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
public class PasswordResetService {

    @Autowired
    @Qualifier("authUserRepository")
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    private static final String REDIS_PREFIX = "password_reset:";
    private static final Duration TOKEN_EXPIRE_TIME = Duration.ofMinutes(30);
    private static final String RESET_URL = "http://localhost:8080/api/auth/reset-password?token=";

    public Mono<String> requestPasswordReset(String email) {
        return Mono.fromCallable(() -> userRepository.findByEmail(email))
                .flatMap(user -> {
                    if (user == null) {
                        return Mono.error(new RuntimeException("该邮箱未注册"));
                    }
                    String token = UUID.randomUUID().toString();
                    String redisKey = REDIS_PREFIX + token;
                    return redisTemplate.opsForValue()
                            .set(redisKey, String.valueOf(user.getUserId()), TOKEN_EXPIRE_TIME)
                            .then(Mono.just(token));
                })
                .flatMap(token -> {
                    sendResetEmail(email, token);
                    return Mono.just("重置链接已发送到您的邮箱");
                });
    }

    public Mono<String> resetPassword(String token, String newPassword) {
        String redisKey = REDIS_PREFIX + token;
        return redisTemplate.opsForValue()
                .get(redisKey)
                .flatMap(userIdStr -> {
                    if (userIdStr == null) {
                        return Mono.error(new RuntimeException("链接已失效或不存在"));
                    }
                    Integer userId = Integer.parseInt(userIdStr);
                    String encodedPassword = passwordEncoder.encode(newPassword);
                    return Mono.fromRunnable(() -> userRepository.updatePassword(userId, encodedPassword))
                            .then(redisTemplate.delete(redisKey))
                            .then(Mono.just("密码重置成功"));
                })
                .switchIfEmpty(Mono.error(new RuntimeException("链接已失效或不存在")));
    }

    private void sendResetEmail(String email, String token) {
        String resetUrl = RESET_URL + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("密码重置请求");
        message.setText("请点击以下链接重置您的密码：\n" + resetUrl + "\n\n该链接30分钟内有效。");
        
        try {
            mailSender.send(message);
            log.info("密码重置邮件已发送到: {}", email);
        } catch (Exception e) {
            log.error("发送邮件失败: {}", e.getMessage());
            throw new RuntimeException("发送邮件失败，请稍后重试");
        }
    }
}