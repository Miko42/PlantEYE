package mp.apk.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import mp.apk.data.entity.ScanCategory

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: ScanCategory): Long

    @Update
    suspend fun updateCategory(category: ScanCategory)

    @Delete
    suspend fun deleteCategory(category: ScanCategory)

    @Query("SELECT * FROM scan_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<ScanCategory>>

    @Query("SELECT * FROM scan_categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): ScanCategory?
}
