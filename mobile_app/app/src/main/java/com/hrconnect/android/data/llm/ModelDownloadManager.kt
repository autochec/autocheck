package com.hrconnect.android.data.llm

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Управляет загрузкой файла модели Gemma 3 270M через системный DownloadManager.
 *
 * Gemma 3 270M IT (4-bit): ~170 МБ
 * HuggingFace: google/gemma-3-270m-it
 */
class ModelDownloadManager(private val context: Context) {

    companion object {
        private const val MODEL_URL =
            "https://huggingface.co/google/gemma-3-270m-it/resolve/main/gemma3-270m-it-int4.task"
        private const val POLL_INTERVAL_MS = 1_000L
    }

    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun isModelDownloaded(): Boolean {
        val file = GemmaReportAnalyzer(context).modelFile
        return file.exists() && file.length() > 0L
    }

    /**
     * Запускает загрузку модели.
     * @return ID загрузки в DownloadManager
     */
    fun startDownload(): Long {
        val destFile = GemmaReportAnalyzer(context).modelFile
        destFile.parentFile?.mkdirs()

        val request = DownloadManager.Request(Uri.parse(MODEL_URL))
            .setTitle("Gemma 3 270M")
            .setDescription("Загрузка локальной AI-модели (~170 МБ)")
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setDestinationUri(Uri.fromFile(destFile))

        return downloadManager.enqueue(request)
    }

    /**
     * Flow с прогрессом загрузки [0..100].
     * Завершается (emit -1) если загрузка упала с ошибкой.
     */
    fun downloadProgress(downloadId: Long): Flow<Int> = flow {
        while (true) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor == null || !cursor.moveToFirst()) {
                emit(-1)
                break
            }

            val statusCol = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val bytesCol = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val totalCol = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

            val status = cursor.getInt(statusCol)
            val downloaded = cursor.getLong(bytesCol)
            val total = cursor.getLong(totalCol)
            cursor.close()

            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    emit(100)
                    break
                }
                DownloadManager.STATUS_FAILED -> {
                    emit(-1)
                    break
                }
                else -> {
                    val progress = if (total > 0) (downloaded * 100 / total).toInt() else 0
                    emit(progress)
                }
            }

            delay(POLL_INTERVAL_MS)
        }
    }.flowOn(Dispatchers.IO)
}
