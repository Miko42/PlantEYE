package mp.apk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mp.apk.data.entity.ScanCategory
import mp.apk.data.entity.ScanItem
import mp.apk.data.repository.ScanRepository
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val scanRepository: ScanRepository
) : ViewModel() {

    val allScans: StateFlow<List<ScanItem>> = scanRepository.getAllScans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mapScans: StateFlow<List<ScanItem>> = scanRepository.getMapScans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteScan(scanItem: ScanItem) {
        viewModelScope.launch {
            scanRepository.deleteScan(scanItem)
        }
    }

}