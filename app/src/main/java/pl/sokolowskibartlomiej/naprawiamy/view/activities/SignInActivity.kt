package pl.sokolowskibartlomiej.naprawiamy.view.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_sign_in.*
import pl.sokolowskibartlomiej.naprawiamy.R
import pl.sokolowskibartlomiej.naprawiamy.utils.PreferencesManager
import pl.sokolowskibartlomiej.naprawiamy.view.adapters.SignInPagerAdapter

class SignInActivity : BaseActivity() {

    private var mBackPressed = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        setSupportActionBar(signInToolbar)

        if (!PreferencesManager.getNightMode() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.WHITE
        }

        viewPager.adapter = SignInPagerAdapter(supportFragmentManager, this@SignInActivity)
        viewPager.currentItem = 0
        viewPager.offscreenPageLimit = 2
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onBackPressed() {
        returnActivity(false)
    }

    fun returnActivity(logged: Boolean) {
        if (logged) {
            startActivity(Intent(this@SignInActivity, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            finish()
        } else doubleBackPressToExit()
    }

    private fun doubleBackPressToExit() {
        if (mBackPressed + 2000 > System.currentTimeMillis()) super.onBackPressed()
        else Toast.makeText(
            baseContext,
            getString(R.string.press_to_exit),
            Toast.LENGTH_SHORT
        ).show()
        mBackPressed = System.currentTimeMillis()
    }
}
