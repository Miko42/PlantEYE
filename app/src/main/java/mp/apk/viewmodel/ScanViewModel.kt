package mp.apk.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mp.apk.PlantIdentificationService
import mp.apk.data.LocationRepository
import mp.apk.data.entity.ScanItem
import mp.apk.data.model.PlantIdentificationResponse
import mp.apk.data.repository.ScanRepository
import java.util.Date
import javax.inject.Inject
import java.io.File
import java.io.FileOutputStream


@HiltViewModel
class ScanViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _scanItem = MutableStateFlow<ScanItem?>(null)
    val scanItem: StateFlow<ScanItem?> = _scanItem.asStateFlow()

    private val _photoUris = MutableStateFlow<List<String>>(emptyList())
    val photoUris: StateFlow<List<String>> = _photoUris

    private val _location = MutableLiveData<Location?>()
    private val location: LiveData<Location?> = _location

    private val _similarImagesList = MutableStateFlow<List<String>>(emptyList())
    val similarImagesList: StateFlow<List<String>> = _similarImagesList.asStateFlow()

    private val _shouldNavigate = MutableStateFlow(false)
    val shouldNavigate: StateFlow<Boolean> = _shouldNavigate

    val selectedPhotoUri: String?
        get() = photoUris.value.firstOrNull()

fun onTakePhoto(uri: Uri, context: Context) {
    viewModelScope.launch {
        val savedPath = saveImageLocally(uri, context)
        savedPath?.let {
            val updatedList = _photoUris.value.toMutableList()
            if (updatedList.size < 3) {
                _photoUris.value = updatedList + it
            } else {
                Log.w("ScanViewModel", "Osiągnięto limit 3 zdjęć.")
            }
        }
        fetchLocation()
    }
}

    private fun saveImageLocally(uri: Uri, context: Context): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val imagesDir = File(context.filesDir, "images")
            if (!imagesDir.exists()) imagesDir.mkdirs()

            val fileName = "photo_${System.currentTimeMillis()}.jpg"
            val file = File(imagesDir, fileName)

            inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            file.absolutePath
        } catch (e: Exception) {
            Log.e("ScanViewModel", "Błąd zapisu zdjęcia lokalnie", e)
            null
        }
    }

    private fun deleteLocalImages() {
        _photoUris.value.forEach { path ->
            val file = File(path)
            if (file.exists()) {
                file.delete()
                Log.d("ScanViewModel", "Usunięto lokalne zdjęcie: ${file.name}")
            }
        }
    }

    private fun fetchLocation() {
        viewModelScope.launch {
            val newLocation = locationRepository.getCurrentLocation()
            _location.postValue(newLocation)
        }
    }

    fun identifyPlant(context: Context) {
        val path = selectedPhotoUri
        if (path == null) {
            Log.e("IdentifyPlant", "Brak wybranego zdjęcia!")
            return
        }

        val uri = Uri.fromFile(File(path))

        Log.d("IdentifyPlant", "Wywołano identyfikację dla URI: $uri, Lokalizacja: $location")

        viewModelScope.launch {
            val locationPair = location.value?.let { loc ->
                Pair(loc.latitude, loc.longitude)
            }

            val result = PlantIdentificationService.identifyPlant(uri, context, locationPair)
            if (result == null) {
                Log.e("IdentifyPlant", "Nie udało się zidentyfikować rośliny")
            } else {
                Log.d("IdentifyPlant", "Zidentyfikowano roślinę: $result")
                parsePlantData(result)
                _shouldNavigate.value = true
            }
        }
    }
    fun resetNavigationFlag() {
        _shouldNavigate.value = false
    }
    private fun parsePlantData(response: PlantIdentificationResponse) {
        viewModelScope.launch {
            try {
                val suggestion = response.result.classification.suggestions.firstOrNull() ?: return@launch
                val plantName = suggestion.details.common_names?.firstOrNull()?.replaceFirstChar { it.uppercaseChar() }
                    ?: suggestion.name
                val latinName = suggestion.name
                val description = suggestion?.details?.description?.value ?: "Description is not available"
                val apiImages = suggestion.similar_images.map { it.url }
                _similarImagesList.value = apiImages

                val userImageUris = _photoUris.value
                val locationData = _location.value

                if (plantName.isNotBlank() && latinName.isNotBlank() && description.isNotBlank() && userImageUris.isNotEmpty()) {
                    val scanItem = ScanItem(
                        scanDate = Date(),
                        userImages = userImageUris.map { it.toString() },
                        plantName = plantName,
                        latinName = latinName,
                        description = description,
                        apiImages = apiImages,
                        userNotes = null,
                        locationLat = locationData?.latitude,
                        locationLon = locationData?.longitude,
                        saveOnMap = locationData != null,
                        categoryId = null
                    )
                    _scanItem.value = scanItem
                } else {
                    Log.w("ScanViewModel", "Brakuje wymaganych danych do stworzenia ScanItem.")
                }
            } catch (e: Exception) {
                Log.e("ScanViewModel", "Błąd parsowania danych rośliny", e)
            }
        }
    }


    fun saveParsedScanItem() {
        _scanItem.value?.let {
            insertScan(it)
            Log.d("ScanViewModel", "ScanItem zapisany do bazy danych.")
        } ?: Log.w("ScanViewModel", "ScanItem jest pusty – nie można zapisać.")
    }

    fun insertScan(scanItem: ScanItem) {
        viewModelScope.launch {
            scanRepository.insertScan(scanItem)
        }
    }

    fun loadScanById(scanId: Int) {
        viewModelScope.launch {
            val scan = scanRepository.getScanById(scanId)
            _scanItem.value = scan
            _photoUris.value = scan?.userImages ?: emptyList()
            _similarImagesList.value = scan?.apiImages.orEmpty()
        }
    }

    fun clearPhotos() {
        _photoUris.value = emptyList()
    }
}
