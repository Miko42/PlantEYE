package mp.apk

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import mp.apk.data.model.PlantIdentificationResponse

object PlantIdentificationService {
    private const val TAG = "PlantIdentificationMock"

    suspend fun identifyPlant(uri: Uri, context: Context, location: Pair<Double, Double>?): PlantIdentificationResponse? = withContext(Dispatchers.IO) {
        Log.d(TAG, "Rozpoczęcie mockowanej identyfikacji rośliny dla URI: $uri")

        delay(1500)

        // Wyciągamy nazwy pakietu i budujemy adresy dla Coil
        val packageName = context.packageName
        val localImage1 = "android.resource://$packageName/drawable/similar_image"
        val localImage2 = "android.resource://$packageName/drawable/similar_image_2"


        val jsonString = """
            {"access_token": "MHPP4qQ4BkRfcrg", "model_version": "plant_id:5.1.1", "custom_id": null, "input": {"latitude": 52, "longitude": 19, "similar_images": true, "images": ["https://plant.id/media/imgs/9a2abfed6d3a4c6f86fa6d53e5a53df2.jpg"], "datetime": "2026-02-23T13:31:40.142378+00:00"}, "result": {"classification": {"suggestions": [{"id": "b241d96601b45be9", "name": "Urtica dioica", "probability": 0.7846, "similar_images": [{"id": "67ed706f7bdd84e1dcd6b184c39b71627999cbad", "url": "$localImage1", "license_name": "CC BY 4.0", "license_url": "https://creativecommons.org/licenses/by/4.0/", "citation": "San Rom\u00e1n Lanza Hector", "similarity": 0.789, "url_small": "$localImage1"}, {"id": "ddafeb20bb3edfc8e110e20eeb3083b0a5445670", "url": "$localImage2", "license_name": "CC BY 4.0", "license_url": "https://creativecommons.org/licenses/by/4.0/", "citation": "Alexis", "similarity": 0.785, "url_small": "$localImage2"}], "details": {"common_names": ["pokrzywa zwyczajna"], "url": "https://pl.wikipedia.org/wiki/Pokrzywa_zwyczajna", "description": {"value": "Pokrzywa \u017cegawka (Urtica urens L.) \u2013 gatunek ro\u015bliny z rodziny pokrzywowatych (Urticaceae Juss.). Gatunek kosmopolityczny, wyst\u0119puj\u0105cy na wszystkich kontynentach (z wyj\u0105tkiem Antarktydy) i na wielu wyspach. W Europie jej zasi\u0119g na p\u00f3\u0142nocy si\u0119ga po Islandi\u0119 i p\u00f3\u0142nocne wybrze\u017ca P\u00f3\u0142wyspu Norweskiego, wyst\u0119puje tak\u017ce na Grenlandii. W Polsce jest archeofitem wyst\u0119puj\u0105cym do\u015b\u0107 pospolicie na ca\u0142ym obszarze kraju", "citation": "https://pl.wikipedia.org/wiki/Pokrzywa_zwyczajna", "license_name": "CC BY-SA 3.0", "license_url": "https://creativecommons.org/licenses/by-sa/3.0/"}, "language": "pl", "entity_id": "b241d96601b45be9"}}, {"id": "57c3cfb98f777aaa", "name": "Urtica urens", "probability": 0.2142, "similar_images": [{"id": "46e4349370df5914a90c00a90de9cb37e99baf00", "url": "$localImage1", "license_name": "CC BY 4.0", "license_url": "https://creativecommons.org/licenses/by/4.0/", "citation": "Alexis", "similarity": 0.729, "url_small": "$localImage1"}, {"id": "b3b6ade5f26613edae9e6701baa2dab1630d1042", "url": "$localImage2", "license_name": "CC BY 4.0", "license_url": "https://creativecommons.org/licenses/by/4.0/", "citation": "Rapha\u00ebl S\u00e9culier", "similarity": 0.706, "url_small": "$localImage2"}], "details": {"common_names": ["pokrzywa \u017cegawka"], "url": "https://pl.wikipedia.org/wiki/Pokrzywa_\u017cegawka", "description": {"value": "Pokrzywa \u017cegawka (Urtica urens L.)...", "citation": "https://pl.wikipedia.org/wiki/Pokrzywa_\u017cegawka", "license_name": "CC BY-SA 3.0", "license_url": "https://creativecommons.org/licenses/by-sa/3.0/"}, "language": "pl", "entity_id": "57c3cfb98f777aaa"}}]}, "is_plant": {"probability": 0.995494, "threshold": 0.5, "binary": true}}, "status": "COMPLETED", "sla_compliant_client": true, "sla_compliant_system": true, "created": 1771853500.142378, "completed": 1771853501.151373}
        """.trimIndent()

        Log.d(TAG, "Zwracanie zmockowanej odpowiedzi z lokalnymi obrazkami: $localImage1")

        return@withContext try {
            val jsonParser = Json { ignoreUnknownKeys = true }
            val response = jsonParser.decodeFromString<PlantIdentificationResponse>(jsonString)
            Log.d(TAG, "Pomyślnie sparsowano mockowaną odpowiedź!")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Błąd parsowania mockowanego JSONa", e)
            null
        }
    }
}