// Lokalizacja: src/mock/java/mp/apk/di/AppModule.kt
package mp.apk.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import mp.apk.data.dao.CategoryDao
import mp.apk.data.dao.ScanDao
import mp.apk.data.database.AppDatabase
import mp.apk.data.entity.Converters
import java.io.File
import java.io.FileOutputStream
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MockAppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        copyMockImagesToInternalStorage(context)

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "scan_database"
        )
            .addTypeConverter(Converters())
            .createFromAsset("database/mocked_database.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideScanDao(database: AppDatabase): ScanDao {
        return database.scanDao()
    }

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    private fun copyMockImagesToInternalStorage(context: Context) {
        val assetManager = context.assets
        val foldersToCopy = listOf("images", "api_images")

        foldersToCopy.forEach { folderName ->
            val destFolder = File(context.filesDir, folderName)
            if (!destFolder.exists()) {
                destFolder.mkdirs()
            }

            val files = assetManager.list(folderName) ?: return@forEach

            for (fileName in files) {
                val destFile = File(destFolder, fileName)
                if (!destFile.exists()) {
                    assetManager.open("$folderName/$fileName").use { inputStream ->
                        FileOutputStream(destFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            }
        }
    }
}