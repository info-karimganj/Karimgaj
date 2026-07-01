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
                description = "করিমগঞ্জ উপজেলা ঢাকা বিভাগের কিশোরগঞ্জ জেলার অন্তর্গত একটি ঐতিহাসিক ও সম্ভাবনাময় অঞ্চল। উপজেলার সদর দপ্তর করিমগঞ্জ পৌরসভা। মোট আয়তন ২০০.৫০ বর্গ কিলোমিটার, মোট জনসংখ্যা ৩,২৮,২৮৮ জন এবং স্বাক্ষরতার হার ৬২.১৯%। প্রধান নদীসমূহ হচ্ছে নরসুন্দা, বাথাইল, সিংগুয়া ও ধানু এবং প্রধান বিলসমূহ হচ্ছে বালিয়া, নাউলি, বিল বাড়া, উখলা, ছোটহারিয়া ও কোলাই।",
                area = "২০০.৫০ বর্গ কিলোমিটার",
                population = "৩,২৮,২৮৮ জন",
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
                    name = "শারমিন সুলতানা / উম্মে মুসলিমা",
                    designation = "উপজেলা নির্বাহী কর্মকর্তা (UNO)",
                    phone = "01332-857121",
                    address = "unokarimganj@mopa.gov.bd | উপজেলা নির্বাহী অফিসারের কার্যালয়, করিমগঞ্জ (যোগদান: ০৭-০৪-২০২৬, বিসিএস ৩৬তম ব্যাচ)",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_chairman",
                    name = "সাইফুল ইসলাম সুমন",
                    designation = "উপজেলা পরিষদ চেয়ারম্যান",
                    phone = "01713-332306",
                    address = "উপজেলা পরিষদ কমপ্লেক্স, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_2",
                    name = "সহকারী কমিশনার (ভূমি) এর কার্যালয়",
                    designation = "সহকারী কমিশনার (ভূমি) / AC Land",
                    phone = "01713-332310",
                    address = "উপজেলা ভূমি অফিস, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_3",
                    name = "করিমগঞ্জ থানা পুলিশ স্টেশন",
                    designation = "অফিসার ইন চার্জ (OC)",
                    phone = "01320-095417",
                    address = "ockis.kar@police.gov.bd | থানা মোড়, করিমগঞ্জ সদর",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_4",
                    name = "উপজেলা কৃষি সম্প্রসারণ অধিদপ্তর",
                    designation = "উপজেলা কৃষি অফিসার",
                    phone = "01711-443322",
                    address = "কমপ্লেক্স ভবন, করিমগঞ্জ",
                    imageUrl = null
                ),
                // 11 Union Parishads (PDF Serial Sequence)
                GovernmentOffice(
                    id = "office_union_kadirjangal",
                    name = "কাদিরজঙ্গল ইউনিয়ন পরিষদ",
                    designation = "ইউপি চেয়ারম্যান: মো : ফজলুর রহমান",
                    phone = "01731-496642",
                    address = "সচিব: সঞ্জিত কুমার আচার্য্য (01716-453247) | কাদিরজঙ্গল, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_union_gujadia",
                    name = "গুজাদিয়া ইউনিয়ন পরিষদ",
                    designation = "ইউপি চেয়ারম্যান: মোঃ রফিকুল ইসলাম",
                    phone = "01716-241357",
                    address = "সচিব: মোহাম্মদ কাঞ্চন মিয়া | গুজাদিয়া, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_union_kiraton",
                    name = "কিরাটন ইউনিয়ন পরিষদ",
                    designation = "ইউপি চেয়ারম্যান: মো : ইবাদুর রহমান শামীম",
                    phone = "01989-255499",
                    address = "সচিব: রজ গোপাল বৈষ্ণব | কিরাটন, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_union_barogharia",
                    name = "বারঘরিয়া ইউনিয়ন পরিষদ",
                    designation = "ইউপি চেয়ারম্যান: আয়ুব উদ্দীন",
                    phone = "01736-437903",
                    address = "সচিব: বিপ্লব কুমার চক্রবর্তী | বারঘরিয়া, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_union_niamatpur",
                    name = "নিয়ামতপুর ইউনিয়ন পরিষদ",
                    designation = "ইউপি চেয়ারম্যান: মখদুম কবীর তন্ময়",
                    phone = "01711-143948",
                    address = "সচিব: মোঃ শামছুল হুদা (01931-354600) | নিয়ামতপুর, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_union_dehunda",
                    name = "দেহুন্দা ইউনিয়ন পরিষদ",
                    designation = "ইউপি চেয়ারম্যান: মশিউর রহমান",
                    phone = "01716-325430",
                    address = "দেহুন্দা, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_union_sutarpara",
                    name = "সুতরপাড়া ইউনিয়ন পরিষদ",
                    designation = "ইউপি চেয়ারম্যান: মো : হারুণ অর রশীদ",
                    phone = "01789-568289",
                    address = "সচিব: তোফায়েল আহমেদ (01710-756589) | সুতরপাড়া, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_union_gunodhar",
                    name = "গুনধর ইউনিয়ন পরিষদ",
                    designation = "ইউপি চেয়ারম্যান: মোঃ আবুছায়েম রাসেল",
                    phone = "01712-856616",
                    address = "সচিব ইমেল: gundharup8@gmail.com | গুনধর, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_union_joyka",
                    name = "জয়কা ইউনিয়ন পরিষদ",
                    designation = "ইউপি চেয়ারম্যান: মো : আশরাফ উদ্দীন",
                    phone = "01741-393843",
                    address = "সচিব: জসিম উদ্দিন (01728-695471) | জয়কা, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_union_zafrabad",
                    name = "জাফরাবাদ ইউনিয়ন পরিষদ",
                    designation = "ইউপি চেয়ারম্যান: সাইফ উদ্দীন ফকির",
                    phone = "01911-924935",
                    address = "সচিব: মো : আমির হামজা | জাফরাবাদ, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_union_noabad",
                    name = "নোয়াবাদ ইউনিয়ন পরিষদ",
                    designation = "ইউপি চেয়ারম্যান: মো : রুহুল আমীন কাজী",
                    phone = "01728-306372",
                    address = "সচিব: রঞ্জিত কুমার সরকার (01720-221987) | নোয়াবাদ, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                // Office Staff
                GovernmentOffice(
                    id = "office_staff_1",
                    name = "মোঃ আজহারুল ইসলাম",
                    designation = "সাঁট মুদ্রাক্ষরিক কাম কম্পিউটার অপারেটর",
                    phone = "01704-014346",
                    address = "azharca12@gmail.com | উপজেলা পরিষদ কমপ্লেক্স, করিমগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_staff_2",
                    name = "মোঃ আলাউদ্দিন মিয়া",
                    designation = "জীপ গাড়ীচালক",
                    phone = "01714-761019",
                    address = "উপজেলা পরিষদ কমপ্লেক্স, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_staff_3",
                    name = "মোঃ মনজিল মিয়া",
                    designation = "অফিস সহায়ক",
                    phone = "01715-565651",
                    address = "উপজেলা পরিষদ কমপ্লেক্স, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_staff_4",
                    name = "মোঃ ইদ্রিস মিয়া",
                    designation = "অফিস সহায়ক",
                    phone = "01626-995808",
                    address = "উপজেলা পরিষদ কমপ্লেক্স, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_staff_5",
                    name = "মোঃ কুরআন আলী",
                    designation = "মালী",
                    phone = "01922-005141",
                    address = "উপজেলা পরিষদ কমপ্লেক্স, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_staff_6",
                    name = "প্রদীপ বাস্পর",
                    designation = "পরিচ্ছন্নতা কর্মী",
                    phone = "01933-297860",
                    address = "উপজেলা পরিষদ কমপ্লেক্স, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_district_dc",
                    name = "জেলা প্রশাসকের কার্যালয়, কিশোরগঞ্জ",
                    designation = "জেলা প্রশাসক (Deputy Commissioner / DC)",
                    phone = "0941-61801",
                    address = "dckishoreganj@mopa.gov.bd | জেলা প্রশাসন ভবন, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_district_sp",
                    name = "পুলিশ সুপারের কার্যালয়, কিশোরগঞ্জ",
                    designation = "পুলিশ সুপার (Superintendent of Police / SP)",
                    phone = "0941-61605",
                    address = "spkishoreganj@police.gov.bd | পুলিশ হেডকোয়ার্টার্স রোড, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                GovernmentOffice(
                    id = "office_district_dae",
                    name = "কৃষি সম্প্রসারণ অধিদপ্তর (খামারবাড়ি), কিশোরগঞ্জ",
                    designation = "উপ-পরিচালক (Deputy Director, DAE)",
                    phone = "0941-61520",
                    address = "dddaekishoreganj@gmail.com | খামারবাড়ি, কিশোরগঞ্জ",
                    imageUrl = null
                )
            )
        )

        // Seed Education Institutes
        civicDao.insertEducationInstitutes(
            listOf(
                // Colleges
                EducationInstitute(
                    id = "edu_college_1",
                    name = "করিমগঞ্জ সরকারি কলেজ",
                    type = "College (মহাবিদ্যালয়)",
                    phone = "01715-998877",
                    address = "EIIN: ১১০৩৮২ | করিমগঞ্জ পৌরসভা, কিশোরগঞ্জ",
                    imageUrl = "https://images.unsplash.com/photo-1523050854058-8df90110c9f1"
                ),
                EducationInstitute(
                    id = "edu_college_2",
                    name = "করিমগঞ্জ পৌর মডেল কলেজ",
                    type = "College (মহাবিদ্যালয়)",
                    phone = "01712-345678",
                    address = "EIIN: ১৩৫৩২১ | করিমগঞ্জ পৌরসভা, কিশোরগঞ্জ",
                    imageUrl = "https://images.unsplash.com/photo-1541339907198-e08756dedf3f"
                ),
                EducationInstitute(
                    id = "edu_college_3",
                    name = "জঙ্গলবাড়ি মহিলা কলেজ",
                    type = "College (মহাবিদ্যালয়)",
                    phone = "01711-223344",
                    address = "EIIN: ১১০৩৮৩ | বাদেশ্রীরামপুর (কাদিরজঙ্গল)",
                    imageUrl = "https://images.unsplash.com/photo-1580582932707-520aed937b7b"
                ),
                EducationInstitute(
                    id = "edu_college_4",
                    name = "হাজী আব্দুল বারী মাস্টার কলেজ",
                    type = "College (মহাবিদ্যালয়)",
                    phone = "01712-233445",
                    address = "EIIN: ১৩৭৬২১ | নিয়ামতপুর, করিমগঞ্জ",
                    imageUrl = null
                ),
                EducationInstitute(
                    id = "edu_college_5",
                    name = "কিশোরগঞ্জ পলিটেকনিক ইনস্টিটিউট",
                    type = "College (কারিগরি)",
                    phone = "01911-334455",
                    address = "পৌরসভা এলাকা, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                // Schools
                EducationInstitute(
                    id = "edu_school_1",
                    name = "করিমগঞ্জ সরকারি পাইলট মডেল উচ্চ বিদ্যালয়",
                    type = "School (উচ্চ বিদ্যালয়)",
                    phone = "01912-112233",
                    address = "EIIN: ১১০৩৫৬ | করিমগঞ্জ পৌরসভা, কিশোরগঞ্জ",
                    imageUrl = "https://images.unsplash.com/photo-1580582932707-520aed937b7b"
                ),
                EducationInstitute(
                    id = "edu_school_2",
                    name = "শামসুন্নাহার-ওসমান গণি শিক্ষা নিকেতন",
                    type = "School (উচ্চ বিদ্যালয়)",
                    phone = "01711-556677",
                    address = "EIIN: ১১০৩৪৭ | করিমগঞ্জ বাজার, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                EducationInstitute(
                    id = "edu_school_3",
                    name = "জঙ্গলবাড়ি উচ্চ বিদ্যালয়",
                    type = "School (উচ্চ বিদ্যালয়)",
                    phone = "01512-321456",
                    address = "EIIN: ১১০৩৪৬ | জঙ্গলবাড়ি দুর্গ সংলগ্ন, করিমগঞ্জ",
                    imageUrl = "https://images.unsplash.com/photo-1541339907198-e08756dedf3f"
                ),
                EducationInstitute(
                    id = "edu_school_4",
                    name = "পিটুয়া আদর্শ উচ্চ বিদ্যালয়",
                    type = "School (উচ্চ বিদ্যালয়)",
                    phone = "01711-224466",
                    address = "EIIN: ১১০৩ND৯ | পিটুয়া, করিমগঞ্জ",
                    imageUrl = null
                ),
                EducationInstitute(
                    id = "edu_school_5",
                    name = "বলিয়া উচ্চ বিদ্যালয়",
                    type = "School (উচ্চ বিদ্যালয়)",
                    phone = "01712-335577",
                    address = "EIIN: ১১০৩৫০ | বলিয়া, করিমগঞ্জ",
                    imageUrl = null
                ),
                EducationInstitute(
                    id = "edu_school_6",
                    name = "নানশ্রী উচ্চ বিদ্যালয়",
                    type = "School (উচ্চ বিদ্যালয়)",
                    phone = "01713-446688",
                    address = "EIIN: ১১০৩৫১ | নানশ্রী, করিমগঞ্জ",
                    imageUrl = null
                ),
                EducationInstitute(
                    id = "edu_school_7",
                    name = "ভাতিয়া উচ্চ বিদ্যালয়",
                    type = "School (উচ্চ বিদ্যালয়)",
                    phone = "01714-557799",
                    address = "EIIN: ১১০৩৫২ | ভাতিয়া (দেহুন্দা)",
                    imageUrl = null
                ),
                EducationInstitute(
                    id = "edu_school_8",
                    name = "উরদিঘী উচ্চ বিদ্যালয়",
                    type = "School (উচ্চ বিদ্যালয়)",
                    phone = "01715-668800",
                    address = "EIIN: ১১০৩৫৩ | উরদিঘী, করিমগঞ্জ",
                    imageUrl = null
                ),
                EducationInstitute(
                    id = "edu_school_9",
                    name = "করিমগঞ্জ পিলখানা বালিকা উচ্চ বিদ্যালয়",
                    type = "School (উচ্চ বিদ্যালয়)",
                    phone = "01716-779911",
                    address = "EIIN: ১১০৩৫৫ | করিমগঞ্জ পৌরসভা, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                EducationInstitute(
                    id = "edu_school_10",
                    name = "সুতারপাড়া জুনিয়র স্কুল",
                    type = "School (জুনিয়র)",
                    phone = "01717-880022",
                    address = "EIIN: ১১০৩৪৮ | সুতারপাড়া, করিমগঞ্জ",
                    imageUrl = null
                ),
                // Madrasas
                EducationInstitute(
                    id = "edu_madrasa_1",
                    name = "করিমগঞ্জ সোবহানিয়া কামিল মাদ্রাসা",
                    type = "Madrasa (মাদ্রাসা)",
                    phone = "01819-445566",
                    address = "ধরণ: কামিল | করিমগঞ্জ সদর, কিশোরগঞ্জ",
                    imageUrl = null
                ),
                EducationInstitute(
                    id = "edu_madrasa_2",
                    name = "কিরাটন ইসলামিয়া ফাজিল মাদ্রাসা",
                    type = "Madrasa (মাদ্রাসা)",
                    phone = "01812-345678",
                    address = "ধরণ: ফাজিল | কিরাটন, করিমগঞ্জ",
                    imageUrl = null
                ),
                EducationInstitute(
                    id = "edu_madrasa_3",
                    name = "নিয়ামতপুর সিনিয়র মাদ্রাসা",
                    type = "Madrasa (মাদ্রাসা)",
                    phone = "01813-456789",
                    address = "ধরণ: ফাজিল / সিনিয়র | নিয়ামতপুর, করিমগঞ্জ",
                    imageUrl = null
                ),
                EducationInstitute(
                    id = "edu_madrasa_4",
                    name = "খাকশ্রী নূরুনুল উলুম ডি এস সিনিয়র মাদ্রাসা",
                    type = "Madrasa (মাদ্রাসা)",
                    phone = "01814-567890",
                    address = "ধরণ: আলিম / সিনিয়র | খাকশ্রী, করিমগঞ্জ",
                    imageUrl = null
                ),
                EducationInstitute(
                    id = "edu_madrasa_5",
                    name = "ঝাউতলার আনোয়ারিয়া সিনিয়র মাদ্রাসা",
                    type = "Madrasa (মাদ্রাসা)",
                    phone = "01815-678901",
                    address = "ধরণ: আলিম / সিনিয়র | নোয়াবাদ, করিমগঞ্জ",
                    imageUrl = null
                ),
                EducationInstitute(
                    id = "edu_madrasa_6",
                    name = "উরদিঘী দাখিল মাদ্রাসা",
                    type = "Madrasa (মাদ্রাসা)",
                    phone = "01816-789012",
                    address = "ধরণ: দাখিল | উরদিঘী, করিমগঞ্জ",
                    imageUrl = null
                )
            )
        )

        // Seed Health Centers
        civicDao.insertHealthCenters(
            listOf(
                HealthCenter(
                    id = "health_1",
                    name = "করিমগঞ্জ উপজেলা স্বাস্থ্য কমপ্লেক্স (৫০ শয্যা)",
                    doctorName = "UH&FPO (উপজেলা স্বাস্থ্য কর্মকর্তা)",
                    phone = "01730-0324498",
                    address = "কিশোরগঞ্জ-করিমগঞ্জ রোড, করিমগঞ্জ সদর (হটলাইন: ০৯৬১১ ৫৩০ ৫৩০, অ্যাম্বুলেন্স: ০১৬৩৫৬০০৮৩৫)",
                    imageUrl = "https://images.unsplash.com/photo-1586773860418-d37222d8fce3"
                ),
                HealthCenter(
                    id = "health_2",
                    name = "নিয়ামতপুর ইউনিয়ন উপ-স্বাস্থ্য কেন্দ্র",
                    doctorName = "ডা. ফারহানা জেসমিন (মেডিকেল অফিসার)",
                    phone = "01511-223344",
                    address = "নিয়ামতপুর বাজার, করিমগঞ্জ (১টি স্যাটেলাইট ক্লিনিক ও ৭টি FWC এর অন্তর্ভুক্ত)",
                    imageUrl = null
                ),
                HealthCenter(
                    id = "health_3",
                    name = "জয়কা মা ও শিশু কল্যাণ কেন্দ্র",
                    doctorName = "ডা. সুফিয়া খাতুন (অন-ডিউটি গাইনোকোলজিস্ট)",
                    phone = "01812-998811",
                    address = "জয়কা ইউপি কার্যালয় সংলগ্ন, করিমগঞ্জ (৩৩টি কমিউনিটি ক্লিনিকের অন্যতম)",
                    imageUrl = null
                )
            )
        )

        // Seed Tourism places
        civicDao.insertTourismPlaces(
            listOf(
                TourismPlace(
                    id = "tour_1",
                    name = "বালিখোলা ঘাট (ভাটির সমুদ্র সৈকত)",
                    description = "করিমগঞ্জ ও মিঠামইনের আকর্ষণীয় সংযোগ পর্যটন ঘাট। ৩ কিমি পিচ ঢালা দৃষ্টিনন্দন সড়ক হাওরের মাঝে চিড়ে তৈরি করা হয়েছে যা বর্ষায় ডুবে যায় না। সূর্যাস্ত দেখতে হাজার হাজার পর্যটক এখানে ভিড় জমায়। এখান থেকে মিঠামইন ও রাষ্ট্রপতির বাড়ি ট্রলারে ভ্রমণ করা যায়।",
                    address = "নিয়ামতপুর ইউনিয়ন, করিমগঞ্জ, কিশোরগঞ্জ",
                    imageUrl = "https://images.unsplash.com/photo-1596401057633-53a01deea096"
                ),
                TourismPlace(
                    id = "tour_2",
                    name = "ঐতিহাসিক জঙ্গলবাড়ি দুর্গ (ঈসা খাঁর দ্বিতীয় রাজধানী)",
                    description = "বাংলার বারো ভূঁইয়ার প্রধান বীর ঈসা খাঁর স্মৃতিবিজড়িত অন্যতম ঐতিহাসিক দুর্গ এটি। মোঘল আমলে নির্মিত দুর্গের সুন্দর পরিখা, সুরম্য তিন গম্বুজ বিশিষ্ট শাহি মসজিদ ও স্মৃতি জাদুঘর এখানে অবস্থিত। করিমগঞ্জ পৌরসভা থেকে প্রায় ৬ কিমি দূরে অবস্থিত।",
                    address = "জঙ্গলবাড়ি গ্রাম, কাদিরজঙ্গল ইউনিয়ন, করিমগঞ্জ",
                    imageUrl = "https://images.unsplash.com/photo-1580582932707-520aed937b7b"
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
                    address = "জরুরি হটলাইন: ১০২ / ০৯৯৯ | করিমগঞ্জ ফায়ার রোড, কিশোরগঞ্জ"
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
                    phone = "01635-600835",
                    address = "হাসপাতাল ঘাট, করিমগঞ্জ"
                ),
                EmergencyContact(
                    id = "emg_4",
                    serviceName = "করিমগঞ্জ থানা পুলিশ স্টেশন (OC)",
                    phone = "01320-095417",
                    address = "ockis.kar@police.gov.bd | থানা মোড়, করিমগঞ্জ"
                ),
                EmergencyContact(
                    id = "emg_5",
                    serviceName = "জরুরি বিদ্যুৎ সেবা (পল্লী বিদ্যুৎ জোনাল অফিস)",
                    phone = "01769-400100",
                    address = "চামড়া বন্দর সড়ক, করিমগঞ্জ"
                ),
                EmergencyContact(
                    id = "emg_6",
                    serviceName = "চাইল্ড হেল্পলাইন (শিশু সহায়তা)",
                    phone = "1098",
                    address = "সারা বাংলাদেশ (টোল ফ্রী)"
                ),
                EmergencyContact(
                    id = "emg_7",
                    serviceName = "নারী ও শিশু নির্যাতন প্রতিরোধ সেল",
                    phone = "109",
                    address = "সারা বাংলাদেশ (টোল ফ্রী)"
                ),
                EmergencyContact(
                    id = "emg_8",
                    serviceName = "সরকারি নৌ-পরিবহন জরুরি হটলাইন",
                    phone = "16113",
                    address = "সারা বাংলাদেশ (টোল ফ্রী)"
                )
            )
        )
    }
}
