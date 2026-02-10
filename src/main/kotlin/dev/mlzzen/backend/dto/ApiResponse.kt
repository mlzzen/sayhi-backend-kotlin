package dev.mlzzen.backend.dto

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
) {
    companion object {
        fun <T> success(data: T, message: String? = null): ApiResponse<T> =
            ApiResponse(success = true, message = message, data = data)

        fun <T> error(message: String): ApiResponse<T> =
            ApiResponse(success = false, message = message, data = null)
    }
}
