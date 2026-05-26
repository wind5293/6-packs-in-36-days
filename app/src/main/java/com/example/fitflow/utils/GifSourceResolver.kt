package com.example.fitflow.utils

import android.content.Context

object GifSourceResolver {

    fun resolve(fileName: String, context: Context): String? {
        val normalized = fileName.trim()
        if (normalized.isEmpty()) return null

        val downloader = GifDownloadManager(context.applicationContext)
        return downloader.getLocalUriIfExists(normalized) ?: GifUrlHelper.getUrl(normalized)
    }

    fun resolveAll(fileNames: List<String>, context: Context): List<String> {
        return fileNames.mapNotNull { resolve(it, context) }
    }
}
