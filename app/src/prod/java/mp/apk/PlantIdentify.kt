package mp.apk

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import mp.apk.data.model.PlantIdentificationResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import mp.apk.BuildConfig
import java.util.Locale


object PlantIdentificationService {
    private const val TAG = "PlantIdentification"
    private val detailsList = listOf("description", "common_names", "url").joinToString(",")


    suspend fun identifyPlant(uri: Uri, context: Context, location: Pair<Double, Double>?): PlantIdentificationResponse? = withContext(Dispatchers.IO) {
        Log.d(TAG, "Rozpoczęcie identyfikacji rośliny")
        val languageCode = Locale.getDefault().language
        val apiUrl = "https://plant.id/api/v3/identification?details=$detailsList&language=$languageCode"

        val client = OkHttpClient()
        val base64Image = uriToBase64(uri, context)

        val apiKey = BuildConfig.API_KEY
        Log.d("ENVtest", "API_KEY: $apiKey")

        if (base64Image == null) {
            Log.e(TAG, "Nie udało się przekonwertować obrazu na Base64")
            return@withContext null
        }

        Log.d(TAG, "Pomyślnie przekonwertowano obraz na Base64")

        val (latitude, longitude) = location ?: (52.0 to 19.0)

        Log.d(TAG, "lokalizacja: $location latitude: $latitude longitude: $longitude")

        val json = JSONObject().apply {
            put("images", listOf("data:image/jpg;base64,$base64Image"))
            put("latitude", latitude)
            put("longitude", longitude)
            put("similar_images", true)
        }

        Log.d(TAG, "Utworzono JSON dla zapytania: $json")

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(apiUrl.toString())
            .post(requestBody)
            .addHeader("Api-Key", apiKey.toString())
            .addHeader("Content-Type", "application/json")
            .build()

        Log.d(TAG, "Wysłanie zapytania do API: $apiUrl")

        return@withContext try {
            val response = client.newCall(request).execute()
            Log.d(TAG, "Otrzymano odpowiedź HTTP: ${response.code}")

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Odpowiedź API: $responseBody")
                val jsonParser = Json { ignoreUnknownKeys = true }
                val plantIdentificationResponse = jsonParser.decodeFromString<PlantIdentificationResponse>(responseBody ?: "")

                Log.d(TAG, "Pomyślnie sparsowano odpowiedź API")
                plantIdentificationResponse
            } else {
                Log.e(TAG, "Błąd API: ${response.code} ${response.message}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Wystąpił błąd podczas komunikacji z API", e)
            null
        }
    }

    private fun uriToBase64(uri: Uri, context: Context): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            Log.d(TAG, "Przeczytano ${bytes?.size ?: 0} bajtów z obrazu")
            bytes?.let { Base64.encodeToString(it, Base64.NO_WRAP) }
        } catch (e: Exception) {
            Log.e(TAG, "Błąd konwersji Uri na Base64", e)
            null
        }
    }
}
