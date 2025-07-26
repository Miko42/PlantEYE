package mp.apk.data.repository

import kotlinx.coroutines.flow.Flow
import mp.apk.data.dao.CategoryDao
import mp.apk.data.dao.ScanDao
import mp.apk.data.entity.ScanCategory
import mp.apk.data.entity.ScanItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanRepository @Inject constructor(
    private val scanDao: ScanDao,
    private val categoryDao: CategoryDao
) {

    fun getAllScans(): Flow<List<ScanItem>> = scanDao.getAllScans()

    fun getMapScans(): Flow<List<ScanItem>> = scanDao.getMapScans()

    suspend fun getScanById(id: Int): ScanItem? = scanDao.getScanById(id)

    fun getScansByCategory(categoryId: Int): Flow<List<ScanItem>> =
        scanDao.getScansByCategory(categoryId)

    suspend fun insertScan(scanItem: ScanItem): Long = scanDao.insertScan(scanItem)

    suspend fun updateScan(scanItem: ScanItem) = scanDao.updateScan(scanItem)

    suspend fun deleteScan(scanItem: ScanItem) = scanDao.deleteScan(scanItem)


    fun getAllCategories(): Flow<List<ScanCategory>> = categoryDao.getAllCategories()

    suspend fun getCategoryById(id: Int): ScanCategory? = categoryDao.getCategoryById(id)

    suspend fun insertCategory(category: ScanCategory): Long = categoryDao.insertCategory(category)

    suspend fun updateCategory(category: ScanCategory) = categoryDao.updateCategory(category)

    suspend fun deleteCategory(category: ScanCategory) = categoryDao.deleteCategory(category)
}
