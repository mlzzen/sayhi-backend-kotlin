package dev.mlzzen.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class JwtConfig {

    @Value("\${jwt.secret}")
    lateinit var jwtSecret: String

    @Value("\${jwt.expiration}")
    lateinit var jwtExpiration: String

    fun getSecret(): String = jwtSecret

    fun getExpiration(): Long = jwtExpiration.toLong()
}
