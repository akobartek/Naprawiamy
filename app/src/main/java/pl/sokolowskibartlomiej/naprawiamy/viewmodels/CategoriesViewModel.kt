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

class CategoriesViewModel(val app: Application) : AndroidViewModel(app) {

    private val repository = NaprawiamyApiRepository()

    val categories = MutableLiveData<List<Category>>()

    fun fetchCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                categories.postValue(repository.getCategories())
            } catch (exc: Throwable) {
                Log.d("Error!", exc.message.toString())
            }
        }
    }
}