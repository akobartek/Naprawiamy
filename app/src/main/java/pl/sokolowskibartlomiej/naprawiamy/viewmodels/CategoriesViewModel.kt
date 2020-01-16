package pl.sokolowskibartlomiej.naprawiamy.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.sokolowskibartlomiej.naprawiamy.apicalls.NaprawiamyApiRepository
import pl.sokolowskibartlomiej.naprawiamy.model.Category
import pl.sokolowskibartlomiej.naprawiamy.utils.PreferencesManager

class CategoriesViewModel(val app: Application) : AndroidViewModel(app) {

    private val repository = NaprawiamyApiRepository()

    val categories = MutableLiveData<List<Category>>()

    fun fetchCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            PreferencesManager.setCategoriesString(listOf())
            try {
                val listOfCategories =
                    if (!PreferencesManager.getCategoriesString().isNullOrEmpty())
                        PreferencesManager.getCategoriesString()
                            ?.split("~")
                            ?.map { Category.createCategoryFromString(it) }
                    else {
                        val numberOfCategories = repository.getCategories()
                        val list = repository.getCategories(0, numberOfCategories)
                        PreferencesManager.setCategoriesString(list)
                        list
                    }
                categories.postValue(listOfCategories)
            } catch (exc: Throwable) {
                Log.d("Error!", exc.message.toString())
            }
        }
    }
}