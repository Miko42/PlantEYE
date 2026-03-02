package mp.apk.data.repository

import android.content.Context
import android.location.Location
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun getCurrentLocation(): Location? {
        Log.d("LocationRepositoryMock", "getCurrentLocation() called (MOCK)")

        delay(500)

        val mockLocation = Location("mock_provider").apply {
            latitude = 52.2318
            longitude = 21.0060
            accuracy = 5.0f
            time = System.currentTimeMillis()
        }

        Log.d("LocationRepositoryMock", "Location retrieved: $mockLocation")
        return mockLocation
    }
}