package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CivicDao {
    @Query("SELECT * FROM upazila_info WHERE id = 'single_doc' LIMIT 1")
    fun getUpazilaInfo(): Flow<UpazilaInfo?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpazilaInfo(info: UpazilaInfo)

    @Query("SELECT * FROM government_offices")
    fun getGovernmentOffices(): Flow<List<GovernmentOffice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGovernmentOffices(items: List<GovernmentOffice>)

    @Query("SELECT * FROM education_institutes")
    fun getEducationInstitutes(): Flow<List<EducationInstitute>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEducationInstitutes(items: List<EducationInstitute>)

    @Query("SELECT * FROM health_centers")
    fun getHealthCenters(): Flow<List<HealthCenter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthCenters(items: List<HealthCenter>)

    @Query("SELECT * FROM tourism_places")
    fun getTourismPlaces(): Flow<List<TourismPlace>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTourismPlaces(items: List<TourismPlace>)

    @Query("SELECT * FROM emergency_contacts")
    fun getEmergencyContacts(): Flow<List<EmergencyContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyContacts(items: List<EmergencyContact>)

    @Query("DELETE FROM government_offices")
    suspend fun clearGovernmentOffices()

    @Query("DELETE FROM education_institutes")
    suspend fun clearEducationInstitutes()

    @Query("DELETE FROM health_centers")
    suspend fun clearHealthCenters()

    @Query("DELETE FROM tourism_places")
    suspend fun clearTourismPlaces()

    @Query("DELETE FROM emergency_contacts")
    suspend fun clearEmergencyContacts()
}

@Database(
    entities = [
        UpazilaInfo::class,
        GovernmentOffice::class,
        EducationInstitute::class,
        HealthCenter::class,
        TourismPlace::class,
        EmergencyContact::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun civicDao(): CivicDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "karimganj_civic_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
