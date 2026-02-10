package dev.mlzzen.backend.security

import dev.mlzzen.backend.config.JwtConfig
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtUtil(
    private val jwtConfig: JwtConfig
) {

    private val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().toByteArray())

    fun generateToken(userId: Long, email: String): String {
        val now = Date()
        val expiration = Date(now.time + jwtConfig.getExpiration())

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            null
        }
    }

    fun getUserIdFromToken(token: String): Long? {
        val claims = validateToken(token) ?: return null
        return claims.subject.toLongOrNull()
    }

    fun getEmailFromToken(token: String): String? {
        val claims = validateToken(token) ?: return null
        return claims.get("email", String::class.java)
    }
}
