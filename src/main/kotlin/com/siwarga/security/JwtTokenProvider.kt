package com.siwarga.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @param:Value("\${jwt.secret}") private val secret: String,
    @param:Value("\${jwt.expiration-ms}") private val expirationMs: Long,
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun generateToken(
        username: String,
        role: String,
    ): String {
        val now = Date()
        return Jwts
            .builder()
            .subject(username)
            .claim("role", role)
            .issuedAt(now)
            .expiration(Date(now.time + expirationMs))
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean = runCatching { parseClaims(token) }.isSuccess

    fun getAuthentication(token: String): Authentication {
        val claims = parseClaims(token)
        val username = claims.subject
        val role = claims["role"] as String
        val authorities = listOf(SimpleGrantedAuthority(role))
        return UsernamePasswordAuthenticationToken(username, null, authorities)
    }

    fun getUsername(token: String): String = parseClaims(token).subject

    private fun parseClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}
