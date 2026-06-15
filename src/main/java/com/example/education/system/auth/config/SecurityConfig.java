package com.example.education.system.auth.config;

import com.example.education.system.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.DispatcherType;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
                .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/schedule/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/images/*/view").permitAll()
                .requestMatchers("/users/profile").authenticated()
                .requestMatchers("/users/*/password").authenticated()
                .requestMatchers("/users/**").hasRole("admin")
                .requestMatchers("/courses/**").hasRole("admin")
                .requestMatchers("/students/**").hasRole("admin")
                .requestMatchers(HttpMethod.GET, "/course-schedules").hasAnyRole("teacher", "admin")
                .requestMatchers(HttpMethod.GET, "/course-schedules/semesters").authenticated()
                .requestMatchers("/course-schedules/**").hasRole("admin")
                .requestMatchers("/course-enrollments").hasRole("student")
                .requestMatchers("/course-enrollments/my").hasRole("student")
                .requestMatchers("/course-enrollments/schedule/**").hasAnyRole("teacher", "admin")
                .requestMatchers("/course-enrollments/*/drop").hasRole("student")
                .requestMatchers("/teacher/**").hasRole("teacher")
                .requestMatchers("/student/**").hasAnyRole("student", "admin")
                .requestMatchers(HttpMethod.GET, "/grades/report").authenticated()
                .requestMatchers("/grades/**").hasAnyRole("teacher", "admin")
                .requestMatchers("/research-projects").hasAnyRole("teacher", "student", "admin")
                .requestMatchers("/research-projects/**").hasAnyRole("teacher", "admin")
                .requestMatchers(HttpMethod.POST, "/project-applications").hasRole("student")
                .requestMatchers(HttpMethod.GET, "/project-applications").hasAnyRole("student", "teacher", "admin")
                .requestMatchers(HttpMethod.DELETE, "/project-applications/*").hasRole("student")
                .requestMatchers(HttpMethod.PUT, "/project-applications/*").hasAnyRole("teacher", "admin")
                .requestMatchers("/innovation-teams").hasAnyRole("student", "admin")
                .requestMatchers("/innovation-teams/**").hasAnyRole("student", "admin")
                .requestMatchers("/team-applications").hasAnyRole("student", "admin")
                .requestMatchers("/team-applications/**").hasAnyRole("student", "admin")
                .requestMatchers("/images").authenticated()
                .requestMatchers("/images/*").authenticated()
                .requestMatchers("/reports/**").hasAnyRole("teacher", "admin")
                .requestMatchers("/dashboard/**").hasRole("admin")
                .requestMatchers("/search/**").authenticated()
                .requestMatchers("/session/**").authenticated()
                .requestMatchers("/chat/**").authenticated()
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