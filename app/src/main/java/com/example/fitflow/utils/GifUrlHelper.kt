package com.example.fitflow.utils

object GifUrlHelper {
    private const val BASE_URL = "https://github.com/wind5293/Exercise_GIFs/releases/download"
    // File dừng ở scorpion_1.gif → v1.0 chứa tất cả file <= "scorpion_1.gif"
    // v1.1 chứa các file > "scorpion_1.gif"
    private const val V1_0_LAST_FILE = "scorpion_1.gif"

    // Some entries in exercises.json reference filenames not present in release assets.
    // Remap those names to canonical files that exist on GitHub releases.
    private val canonicalFileNameMap: Map<String, String> = mapOf(
        "cable_one-arm_tricep_pushdown_(reverse_grip)_1.gif" to "cable_one-arm_tricep_pushdown_1.gif",
        "barbell_bench_press_(close_grip)_1.gif" to "barbell_bench_press_1.gif",
        "dumbbell_shrug_(stability_ball)_1.gif" to "dumbbell_shoulder_shrug_1.gif"
    )

    fun canonicalize(fileName: String): String {
        val normalized = fileName.trim()
        return canonicalFileNameMap[normalized] ?: normalized
    }

    fun getVersion(fileName: String): String {
        val canonical = canonicalize(fileName)
        return if (canonical <= V1_0_LAST_FILE) "v1.0" else "v1.1"
    }

    fun getUrl(fileName: String): String {
        val canonical = canonicalize(fileName)
        val version = getVersion(canonical)
        return "$BASE_URL/$version/$canonical"
    }
}