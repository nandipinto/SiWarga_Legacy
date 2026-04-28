package com.siwarga.security

import com.siwarga.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user =
            userRepository
                .findByUsername(username)
                .orElseThrow { UsernameNotFoundException("User not found: $username") }
        val authority = SimpleGrantedAuthority("ROLE_${user.role.name}")
        return User(user.username, user.password, user.enabled, true, true, true, listOf(authority))
    }
}
