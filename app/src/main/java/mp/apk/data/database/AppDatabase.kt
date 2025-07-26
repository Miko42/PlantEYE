package mp.apk.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import mp.apk.data.dao.CategoryDao
import mp.apk.data.dao.ScanDao
import mp.apk.data.entity.ScanCategory
import mp.apk.data.entity.ScanItem

@Database(entities = [ScanItem::class, ScanCategory::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
    abstract fun categoryDao(): CategoryDao
}