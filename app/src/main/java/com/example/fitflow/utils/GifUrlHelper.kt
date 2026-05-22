package com.example.fitflow.utils

object GifUrlHelper {
    private const val BASE_URL = "https://github.com/wind5293/Exercise_GIFs/releases/download"

    // File dừng ở scorpion_1.gif → v1.0 chứa tất cả file <= "scorpion_1.gif"
    // v1.1 chứa các file > "scorpion_1.gif"
    private const val V1_0_LAST_FILE = "scorpion_1.gif"

    fun getUrl(fileName: String): String {
        val version = if (fileName <= V1_0_LAST_FILE) "v1.0" else "v1.1"
        return "$BASE_URL/$version/$fileName"
    }
}