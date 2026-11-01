package com.lessoncraft.companion

import android.content.Context
import com.lessoncraft.companion.data.AppDatabase
import com.lessoncraft.companion.data.EntitlementStore
import com.lessoncraft.companion.data.LessonRepository

/** Hand-built dependencies: small enough that a framework would be more code than it saves. */
class AppContainer(context: Context) {
    private val db = AppDatabase.create(context)
    val repository = LessonRepository(db.lessons(), db.folders())
    val entitlements = EntitlementStore(context)
}
