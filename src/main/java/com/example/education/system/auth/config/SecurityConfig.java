package com.example.education.system.auth.config;

import com.example.education.system.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtService jwtService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/users/profile").authenticated()
                .requestMatchers("/users/*/password").authenticated()
                .requestMatchers("/users/**").hasRole("admin")
                .requestMatchers("/courses/**").hasRole("admin")
                .requestMatchers("/course-schedules/**").hasRole("admin")
                .requestMatchers("/course-enrollments").hasAnyRole("student", "admin")
                .requestMatchers("/course-enrollments/**").hasAnyRole("student", "admin")
                .requestMatchers("/teacher/**").hasRole("teacher")
                .requestMatchers("/student/**").hasRole("student")
                .requestMatchers("/grades/**").hasAnyRole("teacher", "admin")
                .requestMatchers("/research-projects").hasAnyRole("teacher", "student", "admin")
                .requestMatchers("/research-projects/**").hasAnyRole("teacher", "admin")
                .requestMatchers("/project-applications").hasAnyRole("student", "admin")
                .requestMatchers("/project-applications/**").hasAnyRole("teacher", "admin")
                .requestMatchers("/innovation-teams").hasAnyRole("student", "admin")
                .requestMatchers("/innovation-teams/**").hasAnyRole("student", "admin")
                .requestMatchers("/team-applications").hasAnyRole("student", "admin")
                .requestMatchers("/team-applications/**").hasAnyRole("student", "admin")
                .requestMatchers("/images/**").authenticated()
                .requestMatchers("/reports/**").hasAnyRole("teacher", "admin")
                .requestMatchers("/search/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.addExposedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}