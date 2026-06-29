package com.example

import android.app.Application
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.CivicRepository
import com.example.data.CivicRepositoryImpl
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class KarimganjApplication : Application() {

    // Global coroutine scope bound to the application lifetime for independent cache syncs
    val applicationScope = CoroutineScope(SupervisorJob())

    // Database initializer
    val database by lazy { AppDatabase.getDatabase(this) }

    // Repository singleton accessible from custom ViewModel factory
    val repository: CivicRepository by lazy {
        CivicRepositoryImpl(
            context = this,
            civicDao = database.civicDao(),
            externalScope = applicationScope
        )
    }

    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:527792657241:android:3e82d715cf46c765")
                    .setProjectId("karimganj-app")
                    .setApiKey("AIzaSyDummyKeyForKarimganjCommunityApp")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("KarimganjApplication", "FirebaseApp initialized manually successfully.")
            }
        } catch (e: Exception) {
            Log.e("KarimganjApplication", "Failed to initialize FirebaseApp: ${e.message}")
        }
    }
}
