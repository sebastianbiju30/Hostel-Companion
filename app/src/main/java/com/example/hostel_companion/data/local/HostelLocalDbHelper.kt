package com.example.hostel_companion.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Local SQLite Database Helper for storing student personal notes, checklists,
 * and room reminders directly on the Android device without requiring internet access.
 */
class HostelLocalDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "hostel_companion_local.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_TASKS = "local_tasks"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESC = "description"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_IS_COMPLETED = "is_completed"
        const val COLUMN_CREATED_AT = "created_at"

        @Volatile
        private var instance: HostelLocalDbHelper? = null

        fun getInstance(context: Context): HostelLocalDbHelper {
            return instance ?: synchronized(this) {
                instance ?: HostelLocalDbHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_TASKS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DESC TEXT,
                $COLUMN_CATEGORY TEXT DEFAULT 'General',
                $COLUMN_IS_COMPLETED INTEGER DEFAULT 0,
                $COLUMN_CREATED_AT TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)

        // Seed with practical default tasks for a hostel student
        val now = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        insertSeedTask(db, "Check Weekly Mess Menu", "View special Sunday dinner timings in the app", "Mess", 1, now)
        insertSeedTask(db, "Collect Laundry Package", "Basement counter open until 08:00 PM", "Laundry", 0, now)
        insertSeedTask(db, "Duplicate Room Key Verification", "Verify key tagging with North Block supervisor", "Room", 0, now)
    }

    private fun insertSeedTask(
        db: SQLiteDatabase,
        title: String,
        desc: String,
        category: String,
        completed: Int,
        createdAt: String
    ) {
        val cv = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_DESC, desc)
            put(COLUMN_CATEGORY, category)
            put(COLUMN_IS_COMPLETED, completed)
            put(COLUMN_CREATED_AT, createdAt)
        }
        db.insert(TABLE_TASKS, null, cv)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        onCreate(db)
    }

    fun getAllTasks(): List<LocalTask> {
        val tasks = mutableListOf<LocalTask>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TASKS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_IS_COMPLETED ASC, $COLUMN_ID DESC"
        )

        cursor.use { c ->
            val idIndex = c.getColumnIndexOrThrow(COLUMN_ID)
            val titleIndex = c.getColumnIndexOrThrow(COLUMN_TITLE)
            val descIndex = c.getColumnIndexOrThrow(COLUMN_DESC)
            val catIndex = c.getColumnIndexOrThrow(COLUMN_CATEGORY)
            val completedIndex = c.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)
            val createdAtIndex = c.getColumnIndexOrThrow(COLUMN_CREATED_AT)

            while (c.moveToNext()) {
                tasks.add(
                    LocalTask(
                        id = c.getLong(idIndex),
                        title = c.getString(titleIndex),
                        description = c.getString(descIndex) ?: "",
                        category = c.getString(catIndex) ?: "General",
                        isCompleted = c.getInt(completedIndex) == 1,
                        createdAt = c.getString(createdAtIndex) ?: ""
                    )
                )
            }
        }
        return tasks
    }

    fun insertTask(task: LocalTask): Long {
        val db = writableDatabase
        val now = if (task.createdAt.isNotBlank()) task.createdAt else SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        val cv = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_DESC, task.description)
            put(COLUMN_CATEGORY, task.category)
            put(COLUMN_IS_COMPLETED, if (task.isCompleted) 1 else 0)
            put(COLUMN_CREATED_AT, now)
        }
        return db.insert(TABLE_TASKS, null, cv)
    }

    fun toggleTask(id: Long, isCompleted: Boolean): Int {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COLUMN_IS_COMPLETED, if (isCompleted) 1 else 0)
        }
        return db.update(TABLE_TASKS, cv, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun deleteTask(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_TASKS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }
}
