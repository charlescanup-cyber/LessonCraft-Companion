package com.lessoncraft.companion.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :id")
    fun observe(id: Long): Flow<LessonEntity?>

    @Insert
    suspend fun insert(lesson: LessonEntity): Long

    @Query("UPDATE lessons SET favorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("UPDATE lessons SET folderId = :folderId WHERE id = :id")
    suspend fun move(id: Long, folderId: Long?)

    @Query("DELETE FROM lessons WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<FolderEntity>>

    @Insert
    suspend fun insert(folder: FolderEntity): Long

    @Query("UPDATE folders SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun delete(id: Long)
}
