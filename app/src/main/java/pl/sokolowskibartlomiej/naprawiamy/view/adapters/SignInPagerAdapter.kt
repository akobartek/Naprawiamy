package pl.sokolowskibartlomiej.naprawiamy.view.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import pl.sokolowskibartlomiej.naprawiamy.R
import pl.sokolowskibartlomiej.naprawiamy.view.fragments.SignInFragment

class SignInPagerAdapter(supportFragmentManager: FragmentManager, val context: Context) :
    FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment = SignInFragment.newInstance(position)

    override fun getPageTitle(position: Int): CharSequence? = when (position) {
        0 -> context.getString(R.string.client)
        1 -> context.getString(R.string.specialist)
        else -> ""
    }

    override fun getCount(): Int = 2
}