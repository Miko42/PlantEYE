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

    val allCategories: StateFlow<List<ScanCategory>> = scanRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedScan = MutableStateFlow<ScanItem?>(null)
    val selectedScan: StateFlow<ScanItem?> = _selectedScan

    fun loadScanById(id: Int) {
        viewModelScope.launch {
            _selectedScan.value = scanRepository.getScanById(id)
        }
    }

    fun insertScan(scanItem: ScanItem) {
        viewModelScope.launch {
            scanRepository.insertScan(scanItem)
        }
    }

    fun updateScan(scanItem: ScanItem) {
        viewModelScope.launch {
            scanRepository.updateScan(scanItem)
        }
    }

    fun deleteScan(scanItem: ScanItem) {
        viewModelScope.launch {
            scanRepository.deleteScan(scanItem)
        }
    }

    fun insertCategory(category: ScanCategory) {
        viewModelScope.launch {
            scanRepository.insertCategory(category)
        }
    }

    fun updateCategory(category: ScanCategory) {
        viewModelScope.launch {
            scanRepository.updateCategory(category)
        }
    }

    fun deleteCategory(category: ScanCategory) {
        viewModelScope.launch {
            scanRepository.deleteCategory(category)
        }
    }

    fun getScansByCategory(categoryId: Int): StateFlow<List<ScanItem>> {
        return scanRepository.getScansByCategory(categoryId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
}