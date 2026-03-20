package com.attendance.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

/**
 * Type converters for Room to handle AttendanceStatus enum.
 */
class Converters {
    @TypeConverter
    fun fromAttendanceStatus(status: AttendanceStatus): String = status.name

    @TypeConverter
    fun toAttendanceStatus(value: String): AttendanceStatus =
        AttendanceStatus.valueOf(value)
}

/**
 * Main Room database for the Student Attendance System.
 */
@Database(
    entities = [Student::class, AttendanceRecord::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        private const val DATABASE_NAME = "attendance_db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}
