package mp.apk.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import mp.apk.data.entity.ScanItem


@Dao
interface ScanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scanItem: ScanItem): Long

    @Update
    suspend fun updateScan(scanItem: ScanItem)

    @Delete
    suspend fun deleteScan(scanItem: ScanItem)

    @Query("SELECT * FROM scan_items ORDER BY scanDate DESC")
    fun getAllScans(): Flow<List<ScanItem>>

    @Query("SELECT * FROM scan_items WHERE saveOnMap = 1")
    fun getMapScans(): Flow<List<ScanItem>>

    @Query("SELECT * FROM scan_items WHERE id = :id")
    suspend fun getScanById(id: Int): ScanItem?

    @Query("SELECT * FROM scan_items WHERE categoryId = :categoryId")
    fun getScansByCategory(categoryId: Int): Flow<List<ScanItem>>
}
