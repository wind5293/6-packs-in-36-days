package com.example.fitflow.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class GifDownloadResult(
    val total: Int,
    val downloaded: Int,
    val failedFiles: List<String>
)

class GifDownloadManager(private val context: Context) {

    private val mediaDir: File
        get() = File(context.filesDir, "gifs")

    fun getLocalFile(fileName: String): File {
        return File(mediaDir, fileName)
    }

    fun getLocalUriIfExists(fileName: String): String? {
        val normalized = fileName.trim()
        if (normalized.isEmpty()) return null
        val local = getLocalFile(normalized)
        return if (local.exists() && local.length() > 0L) local.toURI().toString() else null
    }

    suspend fun downloadMissing(
        fileNames: List<String>,
        onProgress: (done: Int, total: Int) -> Unit
    ): GifDownloadResult = withContext(Dispatchers.IO) {
        if (!mediaDir.exists()) {
            mediaDir.mkdirs()
        }

        val targets = fileNames
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        val total = targets.size.coerceAtLeast(1)
        if (targets.isEmpty()) {
            onProgress(1, 1)
            return@withContext GifDownloadResult(total = 0, downloaded = 0, failedFiles = emptyList())
        }

        var done = 0
        val failed = mutableListOf<String>()

        targets.forEach { fileName ->
            val local = getLocalFile(fileName)

            if (local.exists() && local.length() > 0L) {
                done += 1
                onProgress(done, total)
                return@forEach
            }

            val success = downloadWithRetry(fileName, local)
            if (!success) {
                failed += fileName
            }

            done += 1
            onProgress(done, total)
        }

        GifDownloadResult(
            total = targets.size,
            downloaded = targets.size - failed.size,
            failedFiles = failed
        )
    }

    private fun downloadWithRetry(fileName: String, target: File, maxAttempts: Int = 2): Boolean {
        repeat(maxAttempts) { attempt ->
            if (downloadOne(fileName, target)) {
                return true
            }
            Log.w("GifDownload", "Attempt ${attempt + 1} failed for $fileName")
        }
        return false
    }

    private fun downloadOne(fileName: String, target: File): Boolean {
        val temp = File(target.parentFile, "${target.name}.part")
        if (temp.exists()) {
            temp.delete()
        }

        val url = GifUrlHelper.getUrl(fileName)
        var connection: HttpURLConnection? = null

        return try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 12_000
                readTimeout = 20_000
                requestMethod = "GET"
                doInput = true
            }

            connection.connect()
            val response = connection.responseCode
            if (response !in 200..299) {
                Log.w("GifDownload", "HTTP $response for $fileName")
                return false
            }

            connection.inputStream.use { input ->
                FileOutputStream(temp).use { output ->
                    input.copyTo(output)
                }
            }

            if (temp.length() <= 0L) {
                temp.delete()
                return false
            }

            if (target.exists()) {
                target.delete()
            }

            if (!temp.renameTo(target)) {
                temp.copyTo(target, overwrite = true)
                temp.delete()
            }

            target.length() > 0L
        } catch (error: Exception) {
            Log.w("GifDownload", "Download failed for $fileName", error)
            false
        } finally {
            connection?.disconnect()
        }
    }
}
