package com.example.data.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.example.data.db.AppDatabase
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

data class DownloadProgressInfo(
    val downloadManagerId: Long,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val progressPercent: Int = 0,
    val status: DownloadStatus = DownloadStatus.DOWNLOADING,
    val fileName: String = ""
)

class DownloadRepository(private val context: Context) {
    private val downloadDao = AppDatabase.getInstance(context).downloadDao()
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager

    val allDownloads: Flow<List<DownloadItem>> = downloadDao.getAllDownloads()

    suspend fun enqueueDownload(
        url: String,
        suggestedFileName: String? = null,
        mimeType: String? = null
    ): Pair<Long, Long> = withContext(Dispatchers.IO) {
        val cleanName = cleanFileName(suggestedFileName, url)
        val resolvedMime = mimeType?.ifBlank { null }
            ?: getMimeTypeFromUrl(url)
            ?: "video/mp4"

        var downloadManagerId: Long = -1L

        if (downloadManager != null) {
            try {
                val uri = Uri.parse(url)
                val request = DownloadManager.Request(uri).apply {
                    setTitle(cleanName)
                    setDescription("Downloading via AR-Vids")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        "AR-Vids/$cleanName"
                    )
                    setMimeType(resolvedMime)
                    setAllowedOverMetered(true)
                    setAllowedOverRoaming(true)
                }
                downloadManagerId = downloadManager.enqueue(request)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val item = DownloadItem(
            title = cleanName,
            downloadUrl = url,
            localUri = if (downloadManagerId > 0) "id:$downloadManagerId" else null,
            filePath = "Downloads/AR-Vids/$cleanName",
            mimeType = resolvedMime,
            status = DownloadStatus.DOWNLOADING,
            timestamp = System.currentTimeMillis()
        )

        val insertedId = downloadDao.insertDownload(item)

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Download started: $cleanName", Toast.LENGTH_SHORT).show()
        }

        Pair(insertedId, downloadManagerId)
    }

    fun monitorProgress(downloadManagerId: Long, fileName: String): Flow<DownloadProgressInfo> = flow {
        if (downloadManager == null || downloadManagerId <= 0) {
            emit(DownloadProgressInfo(downloadManagerId, status = DownloadStatus.COMPLETED, progressPercent = 100, fileName = fileName))
            return@flow
        }
        var isDone = false
        var attempts = 0
        while (!isDone && attempts < 1200) {
            attempts++
            try {
                val query = DownloadManager.Query().setFilterById(downloadManagerId)
                val cursor = downloadManager.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    val bytesDownloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val totalBytes = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    cursor.close()

                    val percent = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt().coerceIn(0, 100) else 0

                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            emit(DownloadProgressInfo(downloadManagerId, bytesDownloaded, totalBytes, 100, DownloadStatus.COMPLETED, fileName))
                            isDone = true
                        }
                        DownloadManager.STATUS_FAILED -> {
                            emit(DownloadProgressInfo(downloadManagerId, bytesDownloaded, totalBytes, percent, DownloadStatus.FAILED, fileName))
                            isDone = true
                        }
                        else -> {
                            emit(DownloadProgressInfo(downloadManagerId, bytesDownloaded, totalBytes, percent, DownloadStatus.DOWNLOADING, fileName))
                            delay(350)
                        }
                    }
                } else {
                    cursor?.close()
                    delay(350)
                }
            } catch (e: Exception) {
                delay(500)
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun markCompleted(id: Long, finalPath: String? = null, size: Long = 0L) = withContext(Dispatchers.IO) {
        val existing = downloadDao.getDownloadById(id) ?: return@withContext
        downloadDao.updateDownload(
            existing.copy(
                status = DownloadStatus.COMPLETED,
                filePath = finalPath ?: existing.filePath,
                fileSize = if (size > 0L) size else existing.fileSize
            )
        )
    }

    suspend fun deleteDownload(item: DownloadItem) = withContext(Dispatchers.IO) {
        downloadDao.deleteDownload(item)
        try {
            item.filePath?.let { path ->
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, path.removePrefix("Downloads/"))
                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        downloadDao.clearAll()
    }

    private fun cleanFileName(name: String?, url: String): String {
        var base = name?.trim() ?: ""
        if (base.isBlank()) {
            val lastSegment = url.substringBefore("?").substringAfterLast("/")
            base = if (lastSegment.isNotBlank() && lastSegment.length < 50) {
                lastSegment
            } else {
                "AR_Vids_${System.currentTimeMillis()}"
            }
        }
        base = base.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        if (!base.contains(".")) {
            base += ".mp4"
        }
        return base
    }

    private fun getMimeTypeFromUrl(url: String): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        return if (!extension.isNullOrBlank()) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
        } else {
            null
        }
    }
}
