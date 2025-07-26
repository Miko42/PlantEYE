package mp.apk.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_categories")
data class ScanCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)