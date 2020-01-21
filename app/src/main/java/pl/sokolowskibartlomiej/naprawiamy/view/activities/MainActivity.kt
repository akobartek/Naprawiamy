package pl.sokolowskibartlomiej.naprawiamy.view.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import pl.sokolowskibartlomiej.naprawiamy.R
import pl.sokolowskibartlomiej.naprawiamy.utils.PreferencesManager
import pl.sokolowskibartlomiej.naprawiamy.view.fragments.*

class MainActivity : BaseActivity() {

    private var mCurrentFragmentId: Int? = null
    private var mBackPressed = 0L

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_account -> {
                    when (mCurrentFragmentId) {
                        R.id.listingsListFragment ->
                            findNavController(R.id.navHostFragment).navigate(
                                ListingsListFragmentDirections.showAccountFragment()
                            )
                        R.id.settingsFragment ->
                            findNavController(R.id.navHostFragment).navigate(
                                SettingsFragmentDirections.showAccountFragment()
                            )
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_listings -> {
                    when (mCurrentFragmentId) {
                        R.id.accountFragment ->
                            findNavController(R.id.navHostFragment).navigate(
                                AccountFragmentDirections.showListingsListFragment()
                            )
                        R.id.settingsFragment ->
                            findNavController(R.id.navHostFragment).navigate(
                                SettingsFragmentDirections.showListingsListFragment()
                            )
                    }
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_settings -> {
                    when (mCurrentFragmentId) {
                        R.id.accountFragment ->
                            findNavController(R.id.navHostFragment).navigate(
                                AccountFragmentDirections.showSettingsFragment()
                            )
                        R.id.listingsListFragment ->
                            findNavController(R.id.navHostFragment).navigate(
                                ListingsListFragmentDirections.showSettingsFragment()
                            )
                    }
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (PreferencesManager.getNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            window.decorView.systemUiVisibility = 0
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!PreferencesManager.getNightMode() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.WHITE
        }

        if (PreferencesManager.getBearerToken().isNullOrEmpty()) {
            startActivity(Intent(this@MainActivity, SignInActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            finish()
        }

        val navController = (navHostFragment as NavHostFragment? ?: return).navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            mCurrentFragmentId = destination.id
            if (mCurrentFragmentId == R.id.addListingFragment ||
                mCurrentFragmentId == R.id.listingDetailsFragment
            )
                bottomNavView.visibility = View.GONE
            else bottomNavView.visibility = View.VISIBLE
        }

        bottomNavView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    override fun onResume() {
        super.onResume()
        bottomNavView.selectedItemId = R.id.navigation_listings
    }

    override fun onBackPressed() {
        when (mCurrentFragmentId) {
            R.id.listingsListFragment ->
                if ((supportFragmentManager.findFragmentById(R.id.navHostFragment)!!
                        .childFragmentManager.fragments[0] as ListingsListFragment).onBackPressed()
                ) doubleBackPressToExit()
            R.id.accountFragment -> {
                val accountFragment =
                    (supportFragmentManager.findFragmentById(R.id.navHostFragment)!!
                        .childFragmentManager.fragments[0] as AccountFragment)
                if (accountFragment.onBackPressed()) {
                    if (accountFragment.isUserEdited)
                        showUnsavedChangesDialog { accountFragment.disableEditing() }
                    else doubleBackPressToExit()
                }
            }
            R.id.listingDetailsFragment -> {
                val detailsFragment =
                    (supportFragmentManager.findFragmentById(R.id.navHostFragment)!!
                        .childFragmentManager.fragments[0] as ListingDetailsFragment)
                if (detailsFragment.onBackPressed()) {
                    if (detailsFragment.isEditing)
                        showUnsavedChangesDialog { findNavController(R.id.navHostFragment).navigateUp() }
                    else findNavController(R.id.navHostFragment).navigateUp()
                }
            }
            R.id.addListingFragment -> {
                val addListingFragment =
                    (supportFragmentManager.findFragmentById(R.id.navHostFragment)!!
                        .childFragmentManager.fragments[0] as ListingAddFragment)
                if (addListingFragment.onBackPressed()) {
                    if (addListingFragment.isListingEdited)
                        showUnsavedChangesDialog { findNavController(R.id.navHostFragment).navigateUp() }
                    else findNavController(R.id.navHostFragment).navigateUp()
                }
            }
            else -> doubleBackPressToExit()
        }
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

    private fun showUnsavedChangesDialog(discardAction: () -> Unit) =
        AlertDialog.Builder(this@MainActivity)
            .setMessage(R.string.unsaved_changes_dialog_msg)
            .setCancelable(false)
            .setPositiveButton(R.string.discard) { dialog, _ ->
                dialog.dismiss()
                discardAction()
            }
            .setNegativeButton(R.string.keep_editing) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
}
