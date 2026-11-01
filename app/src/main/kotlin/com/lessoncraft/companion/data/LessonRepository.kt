package com.lessoncraft.companion.data

import com.lessoncraft.engine.Lesson
import com.lessoncraft.engine.LessonCodec
import com.lessoncraft.engine.Searchable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Everything the app stores, on the device only. The engine's search runs over what this returns. */
class LessonRepository(private val lessonDao: LessonDao, private val folderDao: FolderDao) {
    val searchables: Flow<List<Searchable>> = lessonDao.observeAll().map { list -> list.map { it.toSearchable() } }
    val folders: Flow<List<FolderEntity>> = folderDao.observeAll()

    fun observe(id: Long): Flow<LessonEntity?> = lessonDao.observe(id)

    suspend fun save(lesson: Lesson, now: Long = System.currentTimeMillis()): Long = lessonDao.insert(
        LessonEntity(
            title = lesson.title,
            subject = lesson.subject.label,
            grade = lesson.grade.label,
            standardCodes = lesson.standard?.let { "${it.georgiaCode} ${it.shortCode}" } ?: "",
            topic = lesson.topic,
            tags = lesson.tags.joinToString("\n"),
            searchText = LessonCodec.searchText(lesson),
            json = LessonCodec.encode(lesson),
            createdAt = now,
        ),
    )

    suspend fun setFavorite(id: Long, favorite: Boolean) = lessonDao.setFavorite(id, favorite)
    suspend fun move(id: Long, folderId: Long?) = lessonDao.move(id, folderId)
    suspend fun delete(id: Long) = lessonDao.delete(id)

    suspend fun createFolder(name: String): Long = folderDao.insert(FolderEntity(name = name.trim(), createdAt = System.currentTimeMillis()))
    suspend fun renameFolder(id: Long, name: String) = folderDao.rename(id, name.trim())
    suspend fun deleteFolder(id: Long) = folderDao.delete(id)
}

fun LessonEntity.toSearchable() = Searchable(
    id = id, title = title, subject = subject, grade = grade, standardCodes = standardCodes, topic = topic,
    tags = tags.split("\n").filter { it.isNotBlank() }, body = searchText, favorite = favorite, folderId = folderId, createdAt = createdAt,
)

fun LessonEntity.decode(): Lesson = LessonCodec.decode(json)
