package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val downloadUrl: String,
    val localUri: String? = null,
    val filePath: String? = null,
    val fileSize: Long = 0L,
    val mimeType: String = "video/mp4",
    val status: DownloadStatus = DownloadStatus.COMPLETED,
    val timestamp: Long = System.currentTimeMillis()
)

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    FAILED
}
