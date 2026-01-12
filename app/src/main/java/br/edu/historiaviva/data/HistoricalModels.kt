package br.edu.historiaviva.data

data class TimelineEvent(
    val year: String,
    val title: String,
    val description: String
)

enum class CharacterCategory(val label: String) {
    Invention("Invenção"),
    ScienceArt("Arte e Ciência"),
    Antiquity("Antiguidade"),
    Leadership("Liderança")
}

data class HistoricalCharacter(
    val id: String,
    val name: String,
    val role: String,
    val category: CharacterCategory,
    val coverImageUrl: String,
    val bio: List<String>,
    val timeline: List<TimelineEvent>,
    val funFacts: List<String>,
    val galleryImageUrls: List<String>,
    val modelAssetFileLocation: String,
    val modelUrl: String? = null,
    val arScaleToUnits: Float = 1.4f,
    val arCenterOriginY: Float = -0.5f
)
