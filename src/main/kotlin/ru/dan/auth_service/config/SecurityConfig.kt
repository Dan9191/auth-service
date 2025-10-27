package ru.dan.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import ru.dan.auth_service.service.JwtFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtFilter: JwtFilter
) {
    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/api/auth/**", "/actuator/**").permitAll()
                    .requestMatchers("/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/webjars/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/user/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_BOOKING_SERVICE")
                    .requestMatchers("/api/user/**").hasAuthority("ROLE_ADMIN")
                    .anyRequest().authenticated()
            }
            .exceptionHandling { exception ->
            exception.accessDeniedHandler { _, response, _ ->
                response.status = 403
                response.contentType = MediaType.APPLICATION_JSON_VALUE
                response.writer.write("""{"message":"Access Denied"}""")
            }
            exception.authenticationEntryPoint { _, response, _ ->
                response.status = 401
                response.contentType = MediaType.APPLICATION_JSON_VALUE
                response.writer.write("""{"message":"Unauthorized"}""")
            }
        }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
