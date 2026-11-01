package com.lessoncraft.companion.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LessonEntity::class, FolderEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessons(): LessonDao
    abstract fun folders(): FolderDao

    companion object {
        fun create(context: Context): AppDatabase = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "companion.db").build()
    }
}
