package pl.sokolowskibartlomiej.naprawiamy.model

data class Category(
    val id: Int?,
    val title: String?,
    val description: String?,
    val parentCategoryId: Int?
)

data class SpecialistCategory(
    val id: Int?,
    val specialistId: Int?,
    val categoryId: Int?
)