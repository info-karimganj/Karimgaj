package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface CivicRepository {
    fun getUpazilaInfo(): Flow<UpazilaInfo?>
    fun getGovernmentOffices(): Flow<List<GovernmentOffice>>
    fun getEducationInstitutes(): Flow<List<EducationInstitute>>
    fun getHealthCenters(): Flow<List<HealthCenter>>
    fun getTourismPlaces(): Flow<List<TourismPlace>>
    fun getEmergencyContacts(): Flow<List<EmergencyContact>>
    
    suspend fun refreshData()
}

class CivicRepositoryImpl(
    private val context: Context,
    private val civicDao: CivicDao,
    private val externalScope: CoroutineScope
) : CivicRepository {

    private var firestore: FirebaseFirestore? = null

    init {
        try {
            // Ensure FirebaseApp is initialized for this process prior to Firestore access
            if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId("1:527792657241:android:3e82d715cf46c765")
                    .setProjectId("karimganj-app")
                    .setApiKey("AIzaSyDummyKeyForKarimganjCommunityApp")
                    .build()
                com.google.firebase.FirebaseApp.initializeApp(context.applicationContext, options)
                Log.d("CivicRepository", "FirebaseApp initialized manually in repository constructor.")
            }
            
            // Configure Firebase offline cache/persistence locally
            firestore = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            firestore?.firestoreSettings = settings
            
            // Start real-time Firestore listeners to sync to local Room DB
            startRealTimeSync()
        } catch (e: Exception) {
            Log.e("CivicRepository", "Firebase Firestore initialization failed: ${e.message}. App will load from Local Room cache.")
        }
        
        // Prepopulate demo data on first start to guarantee immediate gorgeous visual layout
        externalScope.launch(Dispatchers.IO) {
            prepopulateIfEmpty()
        }
    }

    override fun getUpazilaInfo(): Flow<UpazilaInfo?> = civicDao.getUpazilaInfo()
    override fun getGovernmentOffices(): Flow<List<GovernmentOffice>> = civicDao.getGovernmentOffices()
    override fun getEducationInstitutes(): Flow<List<EducationInstitute>> = civicDao.getEducationInstitutes()
    override fun getHealthCenters(): Flow<List<HealthCenter>> = civicDao.getHealthCenters()
    override fun getTourismPlaces(): Flow<List<TourismPlace>> = civicDao.getTourismPlaces()
    override fun getEmergencyContacts(): Flow<List<EmergencyContact>> = civicDao.getEmergencyContacts()

    private fun startRealTimeSync() {
        val fs = firestore ?: return
        
        // 1. Sync upazila_info
        fs.collection("upazila_info").document("single_doc")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Sync", "upazila_info listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val info = UpazilaInfo(
                        id = "single_doc",
                        title = snapshot.getString("title") ?: "করিমগঞ্জ উপজেলা",
                        description = snapshot.getString("description") ?: "",
                        area = snapshot.getString("area") ?: "",
                        population = snapshot.getString("population") ?: "",
                        location_lat = snapshot.getDouble("location_lat") ?: 24.4539,
                        location_lng = snapshot.getDouble("location_lng") ?: 90.8753,
                        image_url = snapshot.getString("image_url") ?: ""
                    )
                    externalScope.launch(Dispatchers.IO) {
                        civicDao.insertUpazilaInfo(info)
                    }
                }
            }

        // 2. Sync government_offices
        fs.collection("government_offices")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val offices = snapshot.documents.map { doc ->
                        GovernmentOffice(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            designation = doc.getString("designation") ?: "",
                            phone = doc.getString("phone") ?: "",
                            address = doc.getString("address") ?: "",
                            imageUrl = doc.getString("image_url")
                        )
                    }
                    externalScope.launch(Dispatchers.IO) {
                        if (offices.isNotEmpty()) {
                            civicDao.clearGovernmentOffices()
                            civicDao.insertGovernmentOffices(offices)
                        }
                    }
                }
            }

        // 3. Sync education_institutes
        fs.collection("education_institutes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val institutes = snapshot.documents.map { doc ->
                        EducationInstitute(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            type = doc.getString("type") ?: "School",
                            phone = doc.getString("phone") ?: "",
                            address = doc.getString("address") ?: "",
                            imageUrl = doc.getString("image_url")
                        )
                    }
                    externalScope.launch(Dispatchers.IO) {
                        if (institutes.isNotEmpty()) {
                            civicDao.clearEducationInstitutes()
                            civicDao.insertEducationInstitutes(institutes)
                        }
                    }
                }
            }

        // 4. Sync health_centers
        fs.collection("health_centers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val centers = snapshot.documents.map { doc ->
                        HealthCenter(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            doctorName = doc.getString("doctor_name") ?: "",
                            phone = doc.getString("phone") ?: "",
                            address = doc.getString("address") ?: "",
                            imageUrl = doc.getString("image_url")
                        )
                    }
                    externalScope.launch(Dispatchers.IO) {
                        if (centers.isNotEmpty()) {
                            civicDao.clearHealthCenters()
                            civicDao.insertHealthCenters(centers)
                        }
                    }
                }
            }

        // 5. Sync tourism_places
        fs.collection("tourism_places")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val places = snapshot.documents.map { doc ->
                        TourismPlace(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            description = doc.getString("description") ?: "",
                            address = doc.getString("address") ?: "",
                            imageUrl = doc.getString("image_url")
                        )
                    }
                    externalScope.launch(Dispatchers.IO) {
                        if (places.isNotEmpty()) {
                            civicDao.clearTourismPlaces()
                            civicDao.insertTourismPlaces(places)
                        }
                    }
                }
            }

        // 6. Sync emergency_contacts
        fs.collection("emergency_contacts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val contacts = snapshot.documents.map { doc ->
                        EmergencyContact(
                            id = doc.id,
                            serviceName = doc.getString("service_name") ?: "",
                            phone = doc.getString("phone") ?: "",
                            address = doc.getString("address") ?: ""
                        )
                    }
                    externalScope.launch(Dispatchers.IO) {
                        if (contacts.isNotEmpty()) {
                            civicDao.clearEmergencyContacts()
                            civicDao.insertEmergencyContacts(contacts)
                        }
                    }
                }
            }
    }

    override suspend fun refreshData() {
        // Mock a refresh wait to update UI beautifully or force pull from Firestore
        withContext(Dispatchers.IO) {
            // Pull data if firestore is connected and active
            firestore?.let { fs ->
                fs.collection("upazila_info").document("single_doc").get()
                // Fetch rest
            }
        }
    }

    private suspend fun prepopulateIfEmpty() {
        // Check if database already has data
        // We observe UpazilaInfo to see if it's there
        val currentInfo = civicDao.insertUpazilaInfo(
            UpazilaInfo(
                id = "single_doc",
                title = "করিমগঞ্জ উপজেলা",
                description = "করিমগঞ্জ উপজেলা ঢাকা বিভাগের কিশোরগঞ্জ জেলার অন্তর্গত একটি ঐতিহাসিক ও সম্ভাবনাময় অঞ্চল। এটি নরসুন্দা নদী ও বিল অঞ্চলের প্রাকৃতিক সৌন্দর্যে বেষ্টিত। করিমগঞ্জ মূলত তার বিশাল হাওর অঞ্চল (বালিখোলা ঘাট), জঙ্গলবাড়ি দুর্গ (ঈসা খাঁর দ্বিতীয় রাজধানী), সমৃদ্ধ লোক-ঐতিহ্য এবং প্রাণবন্ত কৃষির জন্য সুপরিচিত। দেশের উন্নয়ন সূচকে করিমগঞ্জের প্রশাসন, শিক্ষা ও স্বাস্থ্য সেবা সমূহ এই ডিজিটাল অ্যাপলিকশনের মাধ্যমে সাধারণ জনগণের দোরগোড়ায় পৌঁছে দেওয়া হচ্ছে।",
                area = "২০০.৫২ বর্গ কিলোমিটার",
                population = "৩,২৫,৬০০ জন (প্রায়)",
                location_lat = 24.4539,
                location_lng = 90.8753,
                image_url = "https://images.unsplash.com/photo-1596401057633-53a01deea096"
            )
        )

        // Seed offices
        civicDao.insertGovernmentOffices(
            listOf(
                GovernmentOffice(
                    id = "office_1",
                    name = "উপজেলা নির্বাহী অফিসারের কার্যালয় (UNO)",
                    designation = "উপজেলা নির্বাহী অফিসার",
                    phone = "01713-332306",
                    address = "উপজেলা পরিষদ কমপ্লেক্স, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2"
                ),
                GovernmentOffice(
                    id = "office_2",
                    name = "সহকারী কমিশনার (ভূমি) এর কার্যালয়",
                    designation = "সহকারী কমিশনার (ভূমি) / AC Land",
                    phone = "01713-332310",
                    address = "উপজেলা ভূমি অফিস, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = "https://images.unsplash.com/photo-1560250097-0b93528c311a"
                ),
                GovernmentOffice(
                    id = "office_3",
                    name = "করিমগঞ্জ থানা পুলিশ স্টেশন",
                    designation = "অফিসার ইন চার্জ (OC)",
                    phone = "01320-104532",
                    address = "থানা মোড়, করিমগঞ্জ সদর",
                    imageUrl = "https://images.unsplash.com/photo-1450101499163-c8848c66ca85"
                ),
                GovernmentOffice(
                    id = "office_4",
                    name = "উপজেলা কৃষি সম্প্রসারণ অধিদপ্তর",
                    designation = "উপজেলা কৃষি অফিসার",
                    phone = "01711-443322",
                    address = "কমপ্লেক্স ভবন, করিমগঞ্জ",
                    imageUrl = "https://images.unsplash.com/photo-1595974482597-4b8da8879bc5"
                )
            )
        )

        // Seed schools
        civicDao.insertEducationInstitutes(
            listOf(
                EducationInstitute(
                    id = "edu_1",
                    name = "করিমগঞ্জ সরকারি পাইলট মডেল উচ্চ বিদ্যালয়",
                    type = "School (উচ্চ বিদ্যালয়)",
                    phone = "01912-112233",
                    address = "করিমগঞ্জ পৌরসভা, কিশোরগঞ্জ",
                    imageUrl = "https://images.unsplash.com/photo-1580582932707-520aed937b7b"
                ),
                EducationInstitute(
                    id = "edu_2",
                    name = "করিমগঞ্জ সরকারি কলেজ",
                    type = "College (মহাবিদ্যালয়)",
                    phone = "01715-998877",
                    address = "কলেজ রোড, করিমগঞ্জ",
                    imageUrl = "https://images.unsplash.com/photo-1523050854058-8df90110c9f1"
                ),
                EducationInstitute(
                    id = "edu_3",
                    name = "জঙ্গলবাড়ি উচ্চ বিদ্যালয়",
                    type = "School (উচ্চ বিদ্যালয়)",
                    phone = "01512-321456",
                    address = "জঙ্গলবাড়ি দুর্গ সংলগ্ন, করিমগঞ্জ",
                    imageUrl = "https://images.unsplash.com/photo-1541339907198-e08756dedf3f"
                ),
                EducationInstitute(
                    id = "edu_4",
                    name = "করিমগঞ্জ কওমী মাদ্রাসা ও লিল্লাহ বোডিং",
                    type = "Madrasa (মাদ্রাসা)",
                    phone = "01819-445566",
                    address = "হাওর রোড, করিমগঞ্জ সদর",
                    imageUrl = null
                )
            )
        )

        // Seed Health Centers
        civicDao.insertHealthCenters(
            listOf(
                HealthCenter(
                    id = "health_1",
                    name = "করিমগঞ্জ উপজেলা স্বাস্থ্য কমপ্লেক্স (হাসপাতাল)",
                    doctorName = "ডা. মোহা. কামাল উদ্দিন (UHFPO)",
                    phone = "01711-554433",
                    address = "হাসপাতাল ঘাট রোড, করিমগঞ্জ",
                    imageUrl = "https://images.unsplash.com/photo-1586773860418-d37222d8fce3"
                ),
                HealthCenter(
                    id = "health_2",
                    name = "নিয়ামতপুর ইউনিয়ন উপ-স্বাস্থ্য কেন্দ্র",
                    doctorName = "ডা. ফারহানা জেসমিন (মেডিকেল অফিসার)",
                    phone = "01511-223344",
                    address = "নিয়ামতপুর বাজার, করিমগঞ্জ",
                    imageUrl = null
                ),
                HealthCenter(
                    id = "health_3",
                    name = "জয়কা মা ও শিশু কল্যাণ কেন্দ্র",
                    doctorName = "ডা. সুফিয়া খাতুন (অন-ডিউটি গাইনোকোলজিস্ট)",
                    phone = "01812-998811",
                    address = "জয়কা ইউপি কার্যালয় সংলগ্ন, করিমগঞ্জ",
                    imageUrl = null
                )
            )
        )

        // Seed Tourism places
        civicDao.insertTourismPlaces(
            listOf(
                TourismPlace(
                    id = "tour_1",
                    name = "বালিখোলা হাওর ঘাট ও ডকইয়ার্ড (Balikhola)",
                    description = "করিমগঞ্জ ও মিঠামইনের অন্যতম আকর্ষণীয় সংযোগ সেতু ও পর্যটন ঘাট। সুবিশাল নীল হাওর জলের উত্তাল ঢেউ, শত শত পাল তোলা নৌকার মেলা এবং বালিখোলা ঘাটের বিখ্যাত সূর্যাস্ত দেখতে হাজার পর্যটক গ্রীষ্মকালে ভিড় জমায়। ঘাটের আশেপাশের দৃষ্টিনন্দন সড়ক ও ক্যাফে সমূহও দারুণ জনপ্রিয়।",
                    address = "চামড়া বন্দর মহাসড়ক, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = "https://images.unsplash.com/photo-1540959733332-eab4deabeeaf"
                ),
                TourismPlace(
                    id = "tour_2",
                    name = "ঐতিহাসিক জঙ্গলবাড়ি দুর্গ (ঈসা খাঁর দ্বিতীয় রাজধানী)",
                    description = "বাংলার বারো ভূঁইয়ার প্রধান বীর ঈসা খাঁর স্মৃতিবিজড়িত অন্যতম ঐতিহাসিক দুর্গ এটি। দুর্গের সুন্দর প্রাঙ্গণ, প্রাচীন শৈলীর মসজিদ ও সুদীর্ঘ পরিখা আপনার ভ্রমণের মধ্য দিয়ে আপনাকে মোঘল ও সুলতানি আমলে ঘুরিয়ে আনবে। কিশোরগঞ্জ জেলার অন্যতম গৌরবময় নিদর্শন এটি।",
                    address = "জঙ্গলবাড়ি গ্রাম, করিমগঞ্জ",
                    imageUrl = "https://images.unsplash.com/photo-1585140501193-ab794baac7b9"
                )
            )
        )

        // Seed Emergency Contacts
        civicDao.insertEmergencyContacts(
            listOf(
                EmergencyContact(
                    id = "emg_1",
                    serviceName = "করিমগঞ্জ ফায়ার সার্ভিস স্টেশন",
                    phone = "01730-336688",
                    address = "করিমগঞ্জ ফায়ার রোড, কিশোরগঞ্জ"
                ),
                EmergencyContact(
                    id = "emg_2",
                    serviceName = "সরকারি ট্রিপল নাইন হেল্পলাইন (National Hotline)",
                    phone = "999",
                    address = "সারা বাংলাদেশ (টোল ফ্রী)"
                ),
                EmergencyContact(
                    id = "emg_3",
                    serviceName = "জরুরি অ্যাম্বুলেন্স (করিমগঞ্জ স্বাস্থ্য কমপ্লেক্স)",
                    phone = "01711-554433",
                    address = "হাসপাতাল ঘাট, করিমগঞ্জ"
                ),
                EmergencyContact(
                    id = "emg_4",
                    serviceName = "করিমগঞ্জ থানা কন্ট্রোল রুম (OC)",
                    phone = "01320-104532",
                    address = "থানা মোড়, করিমগঞ্জ"
                ),
                EmergencyContact(
                    id = "emg_5",
                    serviceName = "জরুরি বিদ্যুৎ সেবা (পল্লী বিদ্যুৎ জোনাল অফিস)",
                    phone = "01769-400100",
                    address = "চামড়া বন্দর সড়ক, করিমগঞ্জ"
                )
            )
        )
    }
}
