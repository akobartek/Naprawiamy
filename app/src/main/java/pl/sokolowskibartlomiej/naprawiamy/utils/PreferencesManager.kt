package pl.sokolowskibartlomiej.naprawiamy.utils

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import pl.sokolowskibartlomiej.naprawiamy.NaprawiamyApplication
import pl.sokolowskibartlomiej.naprawiamy.model.Category
import pl.sokolowskibartlomiej.naprawiamy.model.User

object PreferencesManager {

    private const val BEARER_TOKEN = "bearer_token"
    private const val USER = "user"
    private const val CATEGORIES = "categories"
    private const val NIGHT_MODE = "night_mode"
    private val sharedPref: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(NaprawiamyApplication.instance)

    fun setBearerToken(bearerToken: String) {
        sharedPref.edit()
            .putString(BEARER_TOKEN, bearerToken)
            .apply()
    }

    fun getBearerToken(): String? = sharedPref.getString(BEARER_TOKEN, "")

    fun setUserString(user: User?) {
        sharedPref.edit()
            .putString(USER, user?.getUserAsString() ?: "")
            .apply()
    }

    fun getUserString(): String? = sharedPref.getString(USER, "")

    fun setCategoriesString(categories: List<Category>) {
        sharedPref.edit()
            .putString(CATEGORIES, categories.joinToString(separator = "~"))
            .apply()
    }

    fun getCategoriesString(): String? = sharedPref.getString(CATEGORIES, "")

    fun isSpecialist(): Boolean = getUserString()!!.split("~")[1] == "true"

    fun getNightMode() = sharedPref.getBoolean(NIGHT_MODE, false)

    fun setNightMode(newValue: Boolean) {
        sharedPref.edit()
            .putBoolean(NIGHT_MODE, newValue)
            .apply()
    }
}