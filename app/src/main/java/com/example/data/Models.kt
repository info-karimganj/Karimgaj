package com.example.data

import androidx.room.*

// Common interface for seamless search filtering in the UI across categories
sealed interface SearchableItem {
    val id: String
    val name: String
    val address: String
    val imageUrl: String?
    val typeLabel: String
    val phone: String?
}

@Entity(tableName = "upazila_info")
data class UpazilaInfo(
    @PrimaryKey val id: String = "single_doc",
    val title: String = "",
    val description: String = "",
    val area: String = "",
    val population: String = "",
    val location_lat: Double = 24.4539,
    val location_lng: Double = 90.8753,
    val image_url: String = ""
)

@Entity(tableName = "government_offices")
data class GovernmentOffice(
    @PrimaryKey override val id: String = "",
    override val name: String = "",
    val designation: String = "",
    override val phone: String = "",
    override val address: String = "",
    @ColumnInfo(name = "image_url") override val imageUrl: String? = null
) : SearchableItem {
    override val typeLabel: String get() = "প্রশাসন"
}

@Entity(tableName = "education_institutes")
data class EducationInstitute(
    @PrimaryKey override val id: String = "",
    override val name: String = "",
    val type: String = "", // e.g., "School", "College", "Madrasa"
    override val phone: String = "",
    override val address: String = "",
    @ColumnInfo(name = "image_url") override val imageUrl: String? = null
) : SearchableItem {
    override val typeLabel: String get() = "শিক্ষা"
}

@Entity(tableName = "health_centers")
data class HealthCenter(
    @PrimaryKey override val id: String = "",
    override val name: String = "",
    @ColumnInfo(name = "doctor_name") val doctorName: String = "",
    override val phone: String = "",
    override val address: String = "",
    @ColumnInfo(name = "image_url") override val imageUrl: String? = null
) : SearchableItem {
    override val typeLabel: String get() = "স্বাস্থ্য"
}

@Entity(tableName = "tourism_places")
data class TourismPlace(
    @PrimaryKey override val id: String = "",
    override val name: String = "",
    val description: String = "",
    override val address: String = "",
    @ColumnInfo(name = "image_url") override val imageUrl: String? = null
) : SearchableItem {
    override val typeLabel: String get() = "পর্যটন"
    override val phone: String? get() = null
}

@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey val id: String = "",
    @ColumnInfo(name = "service_name") val serviceName: String = "",
    val phone: String = "",
    val address: String = ""
)
