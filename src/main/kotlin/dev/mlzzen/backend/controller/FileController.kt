package dev.mlzzen.backend.controller

import dev.mlzzen.backend.security.JwtUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

@RestController
@RequestMapping("/api/files")
class FileController(
    private val jwtUtil: JwtUtil
) {
    @Value("\${app.upload-path:./uploads}")
    private lateinit var uploadPath: String

    @Value("\${app.host:http://localhost:8080}")
    private lateinit var host: String

    private val allowedImageTypes = setOf("image/jpeg", "image/png", "image/gif", "image/webp")
    private val maxFileSize = 10 * 1024 * 1024L // 10MB

    @PostMapping("/upload")
    fun uploadFile(
        @RequestHeader("Authorization") authHeader: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Map<String, Any>> {
        // Validate authorization
        val token = authHeader.substringAfter("Bearer ", "")
        if (jwtUtil.validateToken(token) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Unauthorized"))
        }

        // Validate file
        if (file.isEmpty) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "File is empty"))
        }

        if (file.size > maxFileSize) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "File size exceeds 10MB limit"))
        }

        if (file.contentType !in allowedImageTypes) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "Only image files (JPEG, PNG, GIF, WebP) are allowed"))
        }

        try {
            // Create upload directory if not exists
            val uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize()
            Files.createDirectories(uploadDir)

            // Generate unique filename
            val originalFilename = file.originalFilename ?: "image"
            val extension = originalFilename.substringAfterLast(".", "")
            val uniqueFilename = "${UUID.randomUUID()}.${if (extension.isNotEmpty()) extension else "jpg"}"

            // Save file
            val filePath = uploadDir.resolve(uniqueFilename)
            Files.copy(file.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)

            // Return file URL
            val fileUrl = "$host/uploads/$uniqueFilename"
            return ResponseEntity.ok(mapOf(
                "url" to fileUrl,
                "filename" to uniqueFilename,
                "size" to file.size,
                "contentType" to (file.contentType ?: "image/jpeg")
            ))
        } catch (e: IOException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to upload file: ${e.message}"))
        }
    }

    @DeleteMapping("/upload/{filename}")
    fun deleteFile(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable filename: String
    ): ResponseEntity<Map<String, Any>> {
        // Validate authorization
        val token = authHeader.substringAfter("Bearer ", "")
        if (jwtUtil.validateToken(token) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Unauthorized"))
        }

        try {
            val filePath = Paths.get(uploadPath).resolve(filename)
            val absolutePath = filePath.toAbsolutePath().normalize()

            // Security check: ensure file is within upload directory
            val uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize()
            if (!absolutePath.startsWith(uploadDir)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(mapOf("error" to "Invalid file path"))
            }

            if (Files.exists(absolutePath)) {
                Files.delete(absolutePath)
                return ResponseEntity.ok(mapOf("success" to true))
            }

            return ResponseEntity.notFound().build()
        } catch (e: IOException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to delete file"))
        }
    }
}
