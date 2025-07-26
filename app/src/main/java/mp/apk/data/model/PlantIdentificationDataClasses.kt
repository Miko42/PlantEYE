package mp.apk.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PlantIdentificationResponse(
    val access_token: String,
    val model_version: String,
    val custom_id: String?,
    val input: Input,
    val result: Result
)

@Serializable
data class Input(
    val latitude: Double,
    val longitude: Double,
    val similar_images: Boolean,
    val images: List<String>,
    val datetime: String
)

@Serializable
data class Result(
    val is_plant: IsPlant,
    val classification: Classification,
    val status: String? = null,
    val sla_compliant_client: Boolean? = null,
    val sla_compliant_system: Boolean? = null,
    val created: Double? = null,
    val completed: Double? = null
)


@Serializable
data class IsPlant(
    val probability: Double,
    val threshold: Double,
    val binary: Boolean
)

@Serializable
data class Classification(
    val suggestions: List<Suggestion>
)

@Serializable
data class Suggestion(
    val id: String,
    val name: String,
    val probability: Double,
    val similar_images: List<SimilarImage>,
    val details: PlantDetails
)

@Serializable
data class SimilarImage(
    val id: String,
    val url: String,
    val license_name: String? = null,
    val license_url: String? = null,
    val citation: String? = null,
    val similarity: Double,
    val url_small: String
)

@Serializable
data class PlantDetails(
    val common_names: List<String>? = null,
    val url: String,
    val description: Description?
)

@Serializable
data class Description(
    val value: String? = null,
    val citation: String,
    val license_name: String,
    val license_url: String
)
