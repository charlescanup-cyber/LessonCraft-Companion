package com.lessoncraft.companion.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A folder the teacher made to organize lessons. */
@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
)

/**
 * A saved lesson. The lesson itself is `json` (see LessonCodec); the other columns are what lists and search need without decoding it. Deleting a
 * folder leaves its lessons in the library, unfiled.
 */
@Entity(
    tableName = "lessons",
    foreignKeys = [ForeignKey(entity = FolderEntity::class, parentColumns = ["id"], childColumns = ["folderId"], onDelete = ForeignKey.SET_NULL)],
    indices = [Index("folderId")],
)
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val grade: String,
    @ColumnInfo(name = "standardCodes") val standardCodes: String,
    val topic: String,
    /** Tags joined with a newline. */
    val tags: String,
    val searchText: String,
    val json: String,
    val favorite: Boolean = false,
    val folderId: Long? = null,
    val createdAt: Long,
)
