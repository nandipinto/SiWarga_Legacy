package com.siwarga.config

import com.siwarga.domain.entity.AppUser
import com.siwarga.domain.enums.Role
import com.siwarga.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${admin.username:admin}") private val adminUsername: String,
    @Value("\${admin.password:admin123}") private val adminPassword: String
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(DataInitializer::class.java)

    override fun run(args: ApplicationArguments) {
        if (!userRepository.existsByUsername(adminUsername)) {
            val admin = AppUser(
                username = adminUsername,
                password = passwordEncoder.encode(adminPassword),
                role = Role.ADMIN
            )
            userRepository.save(admin)
            log.info("Default admin user '{}' created", adminUsername)
        }
    }
}
