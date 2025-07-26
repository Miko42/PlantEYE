package mp.apk.data.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity(
    tableName = "scan_items",
    indices = [Index(value = ["categoryId"])],
    foreignKeys = [ForeignKey(
        entity = ScanCategory::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.SET_NULL
    )]
)
@TypeConverters(Converters::class)
data class ScanItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val scanDate: Date,
    val userImages: List<String>,
    val plantName: String,
    val latinName: String,
    val description: String,
    val apiImages: List<String>,
    val userNotes: String? = null,
    val locationLat: Double? = null,
    val locationLon: Double? = null,
    val saveOnMap: Boolean = false,
    val categoryId: Int? = null
)