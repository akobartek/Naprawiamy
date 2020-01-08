package pl.sokolowskibartlomiej.naprawiamy.view.fragments


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import pl.sokolowskibartlomiej.naprawiamy.R
import pl.sokolowskibartlomiej.naprawiamy.utils.PreferencesManager
import pl.sokolowskibartlomiej.naprawiamy.view.activities.SignInActivity

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)

        preferenceManager
            .findPreference<Preference>(getString(R.string.night_mode_key))
            ?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            activity?.let {
                AppCompatDelegate.setDefaultNightMode(
                    if (newValue as Boolean) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
                PreferencesManager.setNightMode(newValue)
            }
            true
        }

        preferenceManager
            .findPreference<Preference>("signOut")
            ?.setOnPreferenceClickListener {
                PreferencesManager.setBearerToken("")
                startActivity(Intent(requireContext(), SignInActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                requireActivity().finish()
                true
            }
    }
}
