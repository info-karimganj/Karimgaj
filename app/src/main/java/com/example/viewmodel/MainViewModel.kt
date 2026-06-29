package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class MainViewModel(private val repository: CivicRepository) : ViewModel() {

    // Selected category filter chip on Home Screen
    private val _selectedCategory = MutableStateFlow("সর্বশেষ")
    val selectedCategory = _selectedCategory.asStateFlow()

    // Real-time incremental search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // State of Swipe-to-Refresh
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    // Upazila Info Document state
    val upazilaInfo: StateFlow<UiState<UpazilaInfo?>> = repository.getUpazilaInfo()
        .map { info ->
            if (info != null) UiState.Success(info) else UiState.Success(null)
        }
        .map { it as UiState<UpazilaInfo?> }
        .catch { emit(UiState.Error("তথ্য লোড করতে ত্রুটি হয়েছে: ${it.message}")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    // Raw emergency contacts stream
    val emergencyContacts: StateFlow<UiState<List<EmergencyContact>>> = repository.getEmergencyContacts()
        .map { list -> UiState.Success(list) }
        .map { it as UiState<List<EmergencyContact>> }
        .catch { emit(UiState.Error("জরুরি নম্বরসমূহ লোড করতে ব্যর্থ: ${it.message}")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    // Merged listing of all searchable administrative, educational, health and tourism sectors
    private val allSearchableItems: Flow<List<SearchableItem>> = combine(
        repository.getGovernmentOffices(),
        repository.getEducationInstitutes(),
        repository.getHealthCenters(),
        repository.getTourismPlaces()
    ) { offices, education, health, tourism ->
        offices + education + health + tourism
    }

    // Dynamic state flow that automatically triggers re-filtering when search or category changes
    val filteredItems: StateFlow<UiState<List<SearchableItem>>> = combine(
        allSearchableItems,
        _selectedCategory,
        _searchQuery
    ) { items, category, query ->
        try {
            val result = items.filter { item ->
                // Category filtering rules
                val matchesCategory = if (category == "সর্বশেষ") {
                    true
                } else {
                    item.typeLabel == category
                }

                // Query filtering rules (check name, address, designation, and phone)
                val matchesQuery = if (query.isBlank()) {
                    true
                } else {
                    item.name.contains(query, ignoreCase = true) ||
                    item.address.contains(query, ignoreCase = true) ||
                    (item.phone?.contains(query) == true) ||
                    (if (item is GovernmentOffice) item.designation.contains(query, ignoreCase = true) else false) ||
                    (if (item is HealthCenter) item.doctorName.contains(query, ignoreCase = true) else false) ||
                    (if (item is EducationInstitute) item.type.contains(query, ignoreCase = true) else false)
                }

                matchesCategory && matchesQuery
            }
            UiState.Success(result)
        } catch (e: Exception) {
            UiState.Error(e.localizedMessage ?: "একটি ত্রুটি ঘটেছে।")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    // Change category filter chip
    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    // Update real-time search pattern
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Force pull-to-refresh sync
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshData()
            } catch (e: Exception) {
                // Gracefully completed
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // Custom Factory to enable constructor parameters in lazy manual DI environment
    class Factory(private val repository: CivicRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
