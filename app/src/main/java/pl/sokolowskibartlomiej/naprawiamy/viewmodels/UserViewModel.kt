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
import pl.sokolowskibartlomiej.naprawiamy.model.User
import pl.sokolowskibartlomiej.naprawiamy.utils.PreferencesManager
import pl.sokolowskibartlomiej.naprawiamy.view.fragments.AccountFragment

class UserViewModel(val app: Application) : AndroidViewModel(app) {

    private val repository = NaprawiamyApiRepository()

    val categories = MutableLiveData<MutableList<Category>>()

    fun fetchSpecialistCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                categories.postValue(repository.getSpecialistCategories().toMutableList())
            } catch (exc: Throwable) {
                Log.e("UserViewModel", exc.toString())
                categories.postValue(mutableListOf())
            }
        }
    }

    fun saveSpecialistCategories(
        oldCategoriesIds: ArrayList<Int>,
        newCategoriesIds: ArrayList<Int>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                oldCategoriesIds.forEach {
                    if (!newCategoriesIds.contains(it)) repository.deleteSpecialistCategory(it)
                }
                newCategoriesIds.forEach {
                    if (!oldCategoriesIds.contains(it)) repository.updateSpecialistCategories(it)
                }
            } catch (exc: Throwable) {
                Log.e("UserViewModel", exc.toString())
            }
        }
    }

    fun updateUserInfo(user: User, fragment: AccountFragment) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isUpdateSuccessful = repository.updateUserInfo(user) != false
                fragment.loadingDialog.dismiss()
                if (isUpdateSuccessful) {
                    PreferencesManager.setUserString(user)
                    fragment.requireActivity().runOnUiThread {
                        fragment.onUpdateSuccessful()
                    }
                } else {
                    fragment.showDataNotSavedDialog()
                }
            } catch (exc: Throwable) {
                Log.e("UserViewModel", exc.message ?: "saveListing failed")
                fragment.requireActivity().runOnUiThread {
                    fragment.loadingDialog.dismiss()
                    fragment.showDataNotSavedDialog()
                }
            }
        }
    }
}