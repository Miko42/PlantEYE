package mp.apk.data
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        Log.d("LocationRepository", "getCurrentLocation() called")

        return suspendCancellableCoroutine { continuation ->
            val task: Task<Location> = fusedLocationClient.lastLocation
            Log.d("LocationRepository", "Requesting last known location")

            task.addOnSuccessListener { location ->
                if (location != null) {
                    Log.d("LocationRepository", "Location retrieved: $location")
                } else {
                    Log.w("LocationRepository", "Location is null")
                }
                continuation.resume(location)
            }

            task.addOnFailureListener { exception ->
                Log.e("LocationRepository", "Failed to get location", exception)
                continuation.resume(null)
            }
        }
    }
}
