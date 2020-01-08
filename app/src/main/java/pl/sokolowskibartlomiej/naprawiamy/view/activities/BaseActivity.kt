package pl.sokolowskibartlomiej.naprawiamy.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import pl.sokolowskibartlomiej.naprawiamy.utils.ViewException
import retrofit2.HttpException

abstract class BaseActivity : ViewException, AppCompatActivity() {
    override fun handleException(e: Throwable): Boolean {
        if ((e is HttpException) && e.code() == 401) {
            startActivity(Intent(this, SignInActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            finish()
            return true
        }
        return false
    }
}