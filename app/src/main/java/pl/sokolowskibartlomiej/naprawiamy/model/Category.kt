package pl.sokolowskibartlomiej.naprawiamy.model

data class Category(
    val id: Int?,
    val title: String?,
    val description: String?,
    val parentCategoryId: Int?
) {
    fun getCategoryAsString(): String =
        "$id&$title&${description ?: "null"}&${parentCategoryId ?: "null"}"

    companion object {
        fun createCategoryFromString(userString: String): Category {
            val category = userString.split("&")
            return Category(
                category[0].toInt(),
                category[1],
                if (category[2] == "null") null else category[2],
                if (category[3] == "null") null else category[3].toInt()
            )
        }
    }
}

data class SpecialistCategory(
    val id: Int?,
    val specialistId: Int?,
    val categoryId: Int?
)