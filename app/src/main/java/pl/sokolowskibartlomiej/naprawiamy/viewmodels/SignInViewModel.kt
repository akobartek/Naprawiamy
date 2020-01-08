package pl.sokolowskibartlomiej.naprawiamy.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.sokolowskibartlomiej.naprawiamy.apicalls.NaprawiamyApiRepository
import pl.sokolowskibartlomiej.naprawiamy.model.User
import pl.sokolowskibartlomiej.naprawiamy.view.fragments.SignInFragment

class SignInViewModel(val app: Application) : AndroidViewModel(app) {

    private val repository = NaprawiamyApiRepository()

    val user = MutableLiveData<User>()

    fun signInWithEmail(
        specialist: Boolean,
        login: String,
        password: String,
        fragment: SignInFragment
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val responseUser = repository.authenticateUser(specialist, login, password)
                user.postValue(responseUser)
            } catch (exc: Throwable) {
                Log.e("SignInViewModel", exc.message.toString())
                fragment.requireActivity().runOnUiThread { fragment.showAccountProblemDialog() }
            }
        }
    }

    fun signUpWithEmail(
        specialist: Boolean,
        login: String,
        password: String,
        fragment: SignInFragment
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val responseUser = repository.signUpUser(specialist, login, password)
                user.postValue(responseUser)
            } catch (exc: Throwable) {
                fragment.requireActivity().runOnUiThread { fragment.showAccountProblemDialog() }
            }
        }
    }
}