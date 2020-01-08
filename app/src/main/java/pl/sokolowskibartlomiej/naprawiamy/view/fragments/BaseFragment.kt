package pl.sokolowskibartlomiej.naprawiamy.view.fragments

import android.content.Intent
import androidx.fragment.app.Fragment
import pl.sokolowskibartlomiej.naprawiamy.utils.ViewException
import pl.sokolowskibartlomiej.naprawiamy.view.activities.SignInActivity
import retrofit2.HttpException

abstract class BaseFragment : ViewException, Fragment() {
    override fun handleException(e: Throwable): Boolean {
        if ((e is HttpException) && e.code() == 401) {
            startActivity(Intent(activity, SignInActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            activity!!.finish()
            return true
        }
        return false
    }
}