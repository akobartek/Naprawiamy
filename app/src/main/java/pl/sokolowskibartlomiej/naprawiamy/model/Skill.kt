package pl.sokolowskibartlomiej.naprawiamy.model

import android.graphics.Color
import pl.sokolowskibartlomiej.naprawiamy.R

class Skill(val category: Category) {

    fun getColor(): Int = Color.parseColor("#4768fd") // @color/indigo

    fun getSelectedTextColor(): Int = Color.WHITE

    fun getText(): String = category.title ?: ""

    override fun equals(other: Any?): Boolean = other is Skill

    // This class isn't used for a key for a collection, overriding hashCode for removing a
    // lint warning
    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    // for DiffCallback
    fun isUiContentEqual(other: Skill) =
        category.id == other.category.id
}