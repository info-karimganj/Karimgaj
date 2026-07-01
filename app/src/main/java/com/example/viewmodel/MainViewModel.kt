package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WeatherInfo(
    val emoji: String,
    val tempText: String,
    val conditionText: String
)

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class MainViewModel(private val repository: CivicRepository) : ViewModel() {

    // Real-time live weather state of Karimganj (Latitude: 24.4533, Longitude: 90.8808)
    private val _weatherState = MutableStateFlow<WeatherInfo?>(null)
    val weatherState = _weatherState.asStateFlow()

    init {
        fetchLiveWeather()
    }

    fun fetchLiveWeather() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val url = java.net.URL("https://api.open-meteo.com/v1/forecast?latitude=24.4533&longitude=90.8808&current=temperature_2m,weather_code")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                
                if (connection.responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = org.json.JSONObject(responseText)
                    val currentJson = json.getJSONObject("current")
                    val temperatureValue = currentJson.getDouble("temperature_2m")
                    val weatherCode = currentJson.getInt("weather_code")
                    
                    val roundedTemp = kotlin.math.round(temperatureValue).toInt()
                    val tempInBangla = roundedTemp.toString().toBanglaDigits()
                    
                    val (emoji, condition) = when (weatherCode) {
                        0 -> "☀️" to "পরিষ্কার আকাশ"
                        1, 2, 3 -> "⛅" to "আংশিক মেঘলা"
                        45, 48 -> "🌫️" to "কুয়াশাচ্ছন্ন"
                        51, 53, 55 -> "🌧️" to "গুড়ি গুড়ি বৃষ্টি"
                        56, 57 -> "🌧️" to "হিমায়িত গুড়ি গুড়ি বৃষ্টি"
                        61, 63, 65 -> "🌧️" to "বৃষ্টিপাত"
                        66, 67 -> "🌧️" to "হিমায়িত বৃষ্টি"
                        71, 73, 75 -> "❄️" to "তুষারপাত"
                        77 -> "❄️" to "তুষার কণা"
                        80, 81, 82 -> "🌧️" to "মুষলধারে বৃষ্টি"
                        85, 86 -> "❄️" to "তুষারপাত"
                        95 -> "⛈️" to "বজ্রঝড়"
                        96, 99 -> "⛈️" to "বজ্রঝড় ও শিলাবৃষ্টি"
                        else -> "☀️" to "রৌদ্রজ্জ্বল"
                    }
                    
                    _weatherState.value = WeatherInfo(emoji, tempInBangla, condition)
                }
            } catch (e: Exception) {
                // Gracefully ignore network errors/timeouts
            }
        }
    }

    private fun String.toBanglaDigits(): String {
        val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        return this.map { char ->
            if (char in '0'..'9') banglaDigits[char - '0'] else char
        }.joinToString("")
    }

    // Selected category filter chip on Home Screen
    private val _selectedCategory = MutableStateFlow("সর্বশেষ")
    val selectedCategory = _selectedCategory.asStateFlow()

    // Global language state (false = Bangla, true = English)
    private val _isEnglish = MutableStateFlow(false)
    val isEnglish = _isEnglish.asStateFlow()

    fun toggleLanguage() {
        _isEnglish.value = !_isEnglish.value
    }

    // Global dark mode state (false = Light mode, true = Dark mode)
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // State-based Navigation System properties
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    private val _activeFullSubPage = MutableStateFlow<String?>(null)
    val activeFullSubPage = _activeFullSubPage.asStateFlow()

    private val _uiCategoryFilter = MutableStateFlow<String?>(null)
    val uiCategoryFilter = _uiCategoryFilter.asStateFlow()

    private val tabHistory = mutableListOf(0)

    fun selectTab(index: Int) {
        if (tabHistory.lastOrNull() != index) {
            tabHistory.remove(index)
            tabHistory.add(index)
        }
        _selectedTab.value = index
        _activeFullSubPage.value = null
        _uiCategoryFilter.value = null
    }

    fun setActiveFullSubPage(page: String?) {
        _activeFullSubPage.value = page
    }

    fun setUiCategoryFilter(category: String?) {
        _uiCategoryFilter.value = category
    }

    fun handleBack(): Boolean {
        if (_activeFullSubPage.value != null) {
            _activeFullSubPage.value = null
            return true
        }
        if (_uiCategoryFilter.value != null) {
            _uiCategoryFilter.value = null
            return true
        }
        if (tabHistory.size > 1) {
            tabHistory.removeAt(tabHistory.size - 1)
            _selectedTab.value = tabHistory.lastOrNull() ?: 0
            return true
        }
        return false
    }

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
            fetchLiveWeather()
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
