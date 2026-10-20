package com.example.data

import com.example.model.Appointment
import com.example.model.ClinicalSpecialty
import com.example.model.HomeoRemedy
import com.example.model.PatientProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class HomeoCareRepository(
    private val firestoreService: ClinicFirestoreService = ClinicFirestoreService()
) {

    // Default Profile initialized with user's details
    private val _patientProfile = MutableStateFlow(
        PatientProfile(
            id = "p_yugabharathi",
            fullName = "Yugabharathi",
            email = "yugabharathi0206@gmail.com",
            mobile = "+91 98401 23456",
            age = 26,
            gender = "Male",
            bloodGroup = "O+",
            city = "Chennai",
            address = "No. 14, 2nd Main Road, Anna Nagar, Chennai - 600040",
            emergencyContactName = "S. Raman",
            emergencyContactPhone = "+91 98401 99887",
            chronicConditions = "Allergic Rhinitis, Occasional Migraine",
            allergies = "Dust mite, Pollen, Sulfa drugs",
            preferredConsultMode = "IN_CLINIC",
            avatarColorIndex = 0,
            healthCardId = "DBHC-2026-8821"
        )
    )
    val patientProfile: StateFlow<PatientProfile> = _patientProfile.asStateFlow()

    // Appointments state
    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    init {
        // Pre-populate with realistic clinic schedule
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        _appointments.value = listOf(
            Appointment(
                id = "HM-2026-1042",
                patientName = "Yugabharathi",
                patientMobile = "+91 98401 23456",
                patientEmail = "yugabharathi0206@gmail.com",
                symptoms = "Recurrent morning sneezing and nasal blockage triggered by cold weather and dust.",
                consultationType = "IN_CLINIC",
                date = todayStr,
                time = "10:30 AM",
                status = "CONFIRMED",
                doctorName = "Dr. Balaji",
                clinicalNotes = "Constitutional Case Taking Scheduled. Advised to note modalities.",
                prescription = "Arsenicum Album 200C - 4 pills once daily in morning on empty tongue for 5 days. Avoid raw onion/coffee 30 min before.",
                createdAt = System.currentTimeMillis() - 86400000L
            ),
            Appointment(
                id = "HM-2026-8924",
                patientName = "Kavitha R.",
                patientMobile = "+91 98412 88471",
                patientEmail = "kavitha.r@gmail.com",
                symptoms = "Chronic migraine with throbbing right-sided headache, nausea, worse with sunlight.",
                consultationType = "ONLINE",
                date = todayStr,
                time = "05:00 PM",
                status = "PENDING",
                doctorName = "Dr. Balaji",
                createdAt = System.currentTimeMillis() - 43200000L
            ),
            Appointment(
                id = "HM-2026-7731",
                patientName = "Suresh Kumar",
                patientMobile = "+91 97900 11223",
                patientEmail = "suresh.k@gmail.com",
                symptoms = "Eczematous dry itchy skin eruptions on flexor creases with winter aggravations.",
                consultationType = "IN_CLINIC",
                date = todayStr,
                time = "11:30 AM",
                status = "COMPLETED",
                doctorName = "Dr. Balaji",
                clinicalNotes = "Patient showed 60% marked improvement in itching. Skin barrier restoring.",
                prescription = "Graphites 30C - 4 pills twice daily for 2 weeks. Calendula mother tincture external wash.",
                createdAt = System.currentTimeMillis() - 172800000L
            )
        )
    }

    suspend fun refreshCloudData() {
        val cloudAppts = firestoreService.fetchAppointments()
        if (cloudAppts.isNotEmpty()) {
            val map = _appointments.value.associateBy { it.id }.toMutableMap()
            for (cloudItem in cloudAppts) {
                val existing = map[cloudItem.id]
                if (existing == null || cloudItem.updatedAt >= existing.updatedAt) {
                    map[cloudItem.id] = cloudItem
                }
            }
            _appointments.value = map.values.sortedByDescending { it.createdAt }
        }
    }

    suspend fun updateProfile(updated: PatientProfile): Boolean {
        _patientProfile.value = updated.copy(lastSyncedAt = System.currentTimeMillis())
        return firestoreService.savePatientProfile(updated)
    }

    suspend fun bookAppointment(
        patientName: String,
        patientMobile: String,
        patientEmail: String,
        symptoms: String,
        consultType: String,
        date: String,
        time: String
    ): Appointment {
        val tokenNum = Random.nextInt(1000, 9999)
        val token = "HM-2026-$tokenNum"

        val newAppt = Appointment(
            id = token,
            appointmentId = token,
            patientName = patientName,
            patientMobile = patientMobile,
            patientEmail = patientEmail,
            symptoms = symptoms,
            consultationType = consultType,
            date = date,
            time = time,
            status = "PENDING",
            doctorName = "Dr. Balaji",
            createdAt = System.currentTimeMillis()
        )

        _appointments.value = listOf(newAppt) + _appointments.value
        firestoreService.saveAppointment(newAppt)
        return newAppt
    }

    suspend fun updateAppointmentStatus(appointmentId: String, newStatus: String) {
        val list = _appointments.value.toMutableList()
        val index = list.indexOfFirst { it.id == appointmentId }
        if (index != -1) {
            val updated = list[index].copy(status = newStatus, updatedAt = System.currentTimeMillis())
            list[index] = updated
            _appointments.value = list
            firestoreService.saveAppointment(updated)
        }
    }

    suspend fun addPrescription(appointmentId: String, prescriptionText: String, clinicalNotes: String) {
        val list = _appointments.value.toMutableList()
        val index = list.indexOfFirst { it.id == appointmentId }
        if (index != -1) {
            val updated = list[index].copy(
                prescription = prescriptionText,
                clinicalNotes = clinicalNotes,
                status = "COMPLETED",
                updatedAt = System.currentTimeMillis()
            )
            list[index] = updated
            _appointments.value = list
            firestoreService.saveAppointment(updated)
        }
    }

    fun getClinicalSpecialties(): List<ClinicalSpecialty> = listOf(
        ClinicalSpecialty(
            id = "skin",
            title = "Skin, Psoriasis & Allergies",
            iconEmoji = "🌸",
            shortDesc = "Permanent relief from eczema, psoriasis, chronic urticaria, and vitiligo.",
            fullDesc = "Classical homeopathy treats skin disorders not as superficial eruptions, but as external manifestations of constitutional imbalance. We never suppress skin lesions with steroid creams; instead, deep-acting individualized remedies stimulate vital defense forces to clear toxins from root level.",
            commonConditions = listOf("Chronic Plaque Psoriasis", "Atopic Eczema & Dermatitis", "Urticaria / Hives", "Vitiligo / Leukoderma", "Acne Vulgaris & Rosacea"),
            classicalApproach = "Thorough constitutional repertorization identifying temperament, thermal reaction (chilly vs hot), and miasmatic background (Psoric/Sycotic/Syphilitic)."
        ),
        ClinicalSpecialty(
            id = "respiratory",
            title = "Respiratory & Chronic Asthma",
            iconEmoji = "🫁",
            shortDesc = "Long-term relief from wheezing, allergic rhinitis, and sinusitis without inhaler dependency.",
            fullDesc = "Dr. Balaji's classical protocol systematically desensitizes hyper-reactive respiratory mucosal membranes against dust, pollen, and temperature fluctuations, restoring normal airway elasticity.",
            commonConditions = listOf("Bronchial Asthma", "Allergic Rhinitis (Morning Sneezing)", "Chronic Sinusitis", "Recurrent Bronchitis", "Nasal Polyps"),
            classicalApproach = "Careful differentiation between spasmodic bronchoconstriction, cold-air aggravations, and post-nasal drip using potentized natural dilutions."
        ),
        ClinicalSpecialty(
            id = "migraine",
            title = "Migraine & Stress Disorders",
            iconEmoji = "🧠",
            shortDesc = "Gentle healing for vascular migraines, tension headaches, insomnia, and anxiety.",
            fullDesc = "Homeopathy recognizes the deep psycho-somatic link. Emotional stress, suppressed grief, or work burnout directly manifest as intense vascular spasms. Classical individualization dissolves recurrent pain patterns.",
            commonConditions = listOf("Hemicrania / One-sided Migraine", "Tension Headaches", "Insomnia / Sleep Latency", "Panic & Anxiety Neurosis", "Cervical Spondylosis Pain"),
            classicalApproach = "Detailed exploration of pain triggers (sun exposure, skipped meals, hormonal cycles, mental exhaustion) and remedy matching."
        ),
        ClinicalSpecialty(
            id = "digestive",
            title = "Digestive Health & IBS",
            iconEmoji = "🥗",
            shortDesc = "Constitutional remedies for GERD, acid reflux, chronic gastritis, and sluggish bowel.",
            fullDesc = "Restores natural gastric motility, optimal digestive fire (Agni), and gut microbiome balance without dependence on antacids or proton-pump inhibitors.",
            commonConditions = listOf("Irritable Bowel Syndrome (IBS)", "Gastroesophageal Reflux (GERD)", "Chronic Gastritis", "Fatty Liver & Sluggish Digestion", "Chronic Constipation"),
            classicalApproach = "Evaluation of food cravings, food intolerances, bloating timelines, and visceral autonomic nerve harmony."
        ),
        ClinicalSpecialty(
            id = "pediatric",
            title = "Pediatric Immunity & Growth",
            iconEmoji = "👶",
            shortDesc = "Sweet pills children love. Safe, non-drowsy natural care for tonsillitis and recurrent infections.",
            fullDesc = "Sweet homeopathic globules are 100% gentle and safe for infants and toddlers. Dr. Balaji boosts natural childhood innate immunity so kids no longer need recurring antibiotics every change of season.",
            commonConditions = listOf("Recurrent Tonsillitis & Adenoids", "Frequent Cough & Cold", "Poor Appetite & Slow Weight Gain", "Dentition Troubles & Colic", "Childhood Behavioral Restlessness"),
            classicalApproach = "Gentle pediatric prescribing based on pregnancy history, milestone markers, thermal state, and childhood temperament."
        ),
        ClinicalSpecialty(
            id = "hormonal",
            title = "Hormonal & Thyroid Balance",
            iconEmoji = "🌿",
            shortDesc = "Natural constitutional therapy for PCOD, thyroid dysregulation, and hormonal hair fall.",
            fullDesc = "Supports pituitary-ovarian and thyroid axes naturally to restore regular menstrual rhythm, facilitate healthy ovulation, and stabilize metabolic basal rates.",
            commonConditions = listOf("PCOS / PCOD with Cystic Ovaries", "Hypothyroidism & Weight Resistance", "Excessive Hair Thinning & Fall", "Irregular / Painful Cycles (Dysmenorrhea)", "Perimenopausal Hot Flashes"),
            classicalApproach = "Holistic regulation restoring natural endocrine homeostasis without synthetic hormone replacement."
        )
    )

    fun getHomeoRemedies(): List<HomeoRemedy> = listOf(
        HomeoRemedy(
            name = "Arnica Montana",
            commonName = "Leopard's Bane / Mountain Daisy",
            category = "Trauma & Muscle Recovery",
            primaryIndications = listOf("Blunt trauma & bruises", "Post-surgical healing", "Physical overexertion & soreness", "Bed feels too hard"),
            keynoteModalities = "Worse from slightest touch, motion, and damp cold; Better lying down flat with head low.",
            clinicalAdvice = "Chief remedy after physical strain, dental procedures, or sprains. Dissolve 4 pills of 30C or 200C under the tongue."
        ),
        HomeoRemedy(
            name = "Pulsatilla Pratensis",
            commonName = "Wind Flower",
            category = "Constitutional & Hormonal",
            primaryIndications = listOf("Shifting wandering pains", "Mild, weeping, affectionate temperament", "Thirstlessness with dry mouth", "Late or irregular menses"),
            keynoteModalities = "Worse in warm stuffy rooms, evening, fatty rich foods; Better in open fresh cool air and gentle motion.",
            clinicalAdvice = "Often prescribed for gentle, yielding personalities who crave reassurance and feel suffocated in closed rooms."
        ),
        HomeoRemedy(
            name = "Nux Vomica",
            commonName = "Poison Nut",
            category = "Digestive & Modern Lifestyle",
            primaryIndications = listOf("Indigestion from spicy food & coffee", "Gastric irritability & heartburn", "Workaholic, irritable, ambitious", "Frequent ineffectual urge for stool"),
            keynoteModalities = "Worse early morning (3 AM), cold air, mental exertion; Better warm drinks, moist heat, undisturbed rest.",
            clinicalAdvice = "Prime remedy for urban executives with sedentary habits, coffee overconsumption, and digestive sluggishness."
        ),
        HomeoRemedy(
            name = "Arsenicum Album",
            commonName = "White Oxide of Arsenic",
            category = "Respiratory & Vital Exhaustion",
            primaryIndications = listOf("Midnight asthma & wheezing (12–2 AM)", "Burning pains relieved by heat", "Intense anxiety, neatness & restlessness", "Sips small quantities of water frequently"),
            keynoteModalities = "Worse midnight to 2 AM, cold drinks, wet weather; Better warm wraps, hot applications, sitting up in bed.",
            clinicalAdvice = "Exceptional remedy in acute allergic rhinitis with acrid watery nasal discharge that excoriates upper lip."
        ),
        HomeoRemedy(
            name = "Lycopodium Clavatum",
            commonName = "Club Moss",
            category = "Liver & Chronic Metabolism",
            primaryIndications = listOf("Excessive abdominal flatulence", "Bloating immediately after small meal", "Right-to-left symptom progression", "Craves warm sweets and hot drinks"),
            keynoteModalities = "Worse 4 PM to 8 PM, cold food, tight clothing around waist; Better warm food, unfastening garments.",
            clinicalAdvice = "Key constitutional medicine for intellectual individuals with sluggish hepatic function and digestive gas."
        ),
        HomeoRemedy(
            name = "Belladonna",
            commonName = "Deadly Nightshade",
            category = "Acute Inflammatory & Migraine",
            primaryIndications = listOf("Sudden violent onset of symptoms", "Throbbing congestive vascular headache", "Flushed red face with dilated pupils", "High fever with radiating hot skin"),
            keynoteModalities = "Worse noise, jar, bright light, lying down; Better sitting semi-erect, quiet darkened room.",
            clinicalAdvice = "Unmatched speed in acute vascular migraines where every footstep or vibration worsens the cerebral throbbing."
        ),
        HomeoRemedy(
            name = "Rhus Toxicodendron",
            commonName = "Poison Ivy",
            category = "Joints, Tendons & Skin",
            primaryIndications = listOf("Joint stiffness worse on first motion", "Restless limbs needing continuous change of position", "Skin eruptions with vesicular intense itching", "Ailments from getting drenched in rain"),
            keynoteModalities = "Worse rest, first movement, cold wet weather, midnight; Better continuous gentle motion, dry warm heat.",
            clinicalAdvice = "The classic 'rusty hinge' remedy. Great for morning stiffness in knees and lower back."
        )
    )
}
