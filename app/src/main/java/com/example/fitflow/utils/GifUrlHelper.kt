package com.example.fitflow.utils

object GifUrlHelper {
    fun getUrl(fileName: String): String {
        return "file:///android_asset/gifs/$fileName"
    }
}
