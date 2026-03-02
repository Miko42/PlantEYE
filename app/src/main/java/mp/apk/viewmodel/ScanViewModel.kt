package mp.apk.viewmodel

import android.content.Context
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import mp.apk.PlantIdentificationService
import mp.apk.R
import mp.apk.data.entity.ScanItem
import mp.apk.data.model.PlantIdentificationResponse
import mp.apk.data.repository.LocationRepository
import mp.apk.data.repository.ScanRepository
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val scanRepository: ScanRepository,
    @ApplicationContext private val context: Context
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

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()


    sealed class UiEvent {
        object ShowPhotoLimitReached : UiEvent()
        object ShowNoPhotoSelected : UiEvent()
    }
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

                _uiEvent.send(UiEvent.ShowPhotoLimitReached)
                File(it).delete()
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
            viewModelScope.launch {
                _uiEvent.send(UiEvent.ShowNoPhotoSelected)
            }
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

                val isLowProbability = suggestion.probability < 0.5
                val probabilityPercent = (suggestion.probability * 100).toInt()



                val plantName = if (isLowProbability) {
                    context.getString(R.string.plant_not_recognized_title)
                } else {
                    suggestion.details.common_names?.firstOrNull()?.replaceFirstChar { it.uppercaseChar() }
                        ?: suggestion.name
                }


                val latinName = if (isLowProbability) {
                    ""
                } else {
                    suggestion.name
                }

                val description = if (isLowProbability) {
                    context.getString(R.string.low_probability_message, probabilityPercent)
                } else {
                    suggestion.details.description?.value ?: "Description is not available"
                }

                val apiImages = if (isLowProbability) {
                    emptyList()
                } else {
                    val urls = suggestion.similar_images.map { it.url }

                    urls.mapNotNull { url ->
                        downloadAndSaveApiImage(url)
                    }
                }

                _similarImagesList.value = apiImages

                val userImageUris = if (mp.apk.BuildConfig.FLAVOR == "mock") {
                    val currentPhotos = _photoUris.value

                    if (currentPhotos.size > 2) {
                        val photosToDelete = currentPhotos.drop(2)

                        photosToDelete.forEach { path ->
                            try {
                                val file = java.io.File(path)
                                if (file.exists()) {
                                    val deleted = file.delete()
                                    if (deleted) {
                                        Log.d("ScanViewModel", "Usunięto nadmiarowe zdjęcie z dysku: ${file.name}")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("ScanViewModel", "Błąd podczas usuwania nadmiarowego zdjęcia", e)
                            }
                        }
                    }
                    currentPhotos.take(2)
                } else {
                    _photoUris.value
                }
                _photoUris.value = userImageUris

                val locationData = _location.value

                if (plantName.isNotBlank() && userImageUris.isNotEmpty()) {
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
                    _isSaved.value = false
                } else {
                    Log.w("ScanViewModel", "Brakuje wymaganych danych do stworzenia ScanItem.")
                }
            } catch (e: Exception) {
                Log.e("ScanViewModel", "Błąd parsowania danych rośliny", e)
            }
        }
    }


    fun saveParsedScanItem() {
        if (_isSaved.value) {
            Log.d("ScanViewModel", "Ten ScanItem został już zapisany.")
            return
        }

        _scanItem.value?.let {
            insertScan(it)
            _isSaved.value = true
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


    private suspend fun downloadAndSaveApiImage(imageUrl: String): String? = withContext(Dispatchers.IO) {
        if (imageUrl.startsWith("android.resource://")) {
            return@withContext imageUrl
        }

        return@withContext try {

            val imagesDir = File(context.filesDir, "api_images")
            if (!imagesDir.exists()) imagesDir.mkdirs()

            val fileName = "api_photo_${System.currentTimeMillis()}_${imageUrl.hashCode()}.jpg"
            val file = File(imagesDir, fileName)

            URL(imageUrl).openStream().use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            Log.d("ScanViewModel", "Zapisano pobrane zdjęcie API: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e("ScanViewModel", "Błąd pobierania zdjęcia z API: $imageUrl", e)
            null
        }
    }
}


