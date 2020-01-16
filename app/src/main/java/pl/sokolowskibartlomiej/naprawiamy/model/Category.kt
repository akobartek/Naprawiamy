package pl.sokolowskibartlomiej.naprawiamy.model

data class Category(
    val id: Int?,
    val title: String?,
    val description: String?,
    val parentCategoryId: Int?
) {
    fun getCategoryAsString(): String =
        "$id&$title&${description ?: ""}&${parentCategoryId ?: ""}"

    companion object {
        fun createCategoryFromString(userString: String): Category {
            val category = userString.split("&")
            return Category(
                category[0].toInt(), category[1], category[2], category[3].toInt()
            )
        }
    }
}

data class SpecialistCategory(
    val id: Int?,
    val specialistId: Int?,
    val categoryId: Int?
)