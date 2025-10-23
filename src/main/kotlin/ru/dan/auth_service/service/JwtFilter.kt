package ru.dan.auth_service.service

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtFilter(private val jwtUtil: JwtUtil) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            if (jwtUtil.validateToken(token)) {
                val username = jwtUtil.getUsernameFromToken(token)
                val roles = jwtUtil.getRolesFromToken(token)

                val authorities = roles.map { SimpleGrantedAuthority("ROLE_$it") }

                val authentication = UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    authorities
                )

                SecurityContextHolder.getContext().authentication = authentication
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token")
                return
            }
        }
        filterChain.doFilter(request, response)
    }
}