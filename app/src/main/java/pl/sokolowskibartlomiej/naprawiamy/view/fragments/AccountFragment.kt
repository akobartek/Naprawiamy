package pl.sokolowskibartlomiej.naprawiamy.view.fragments

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import kotlinx.android.synthetic.main.content_account.*
import kotlinx.android.synthetic.main.content_account.view.*
import kotlinx.android.synthetic.main.fragment_account.view.*
import pl.sokolowskibartlomiej.naprawiamy.R
import pl.sokolowskibartlomiej.naprawiamy.model.User
import pl.sokolowskibartlomiej.naprawiamy.utils.PreferencesManager
import pl.sokolowskibartlomiej.naprawiamy.utils.disable
import pl.sokolowskibartlomiej.naprawiamy.utils.enable
import pl.sokolowskibartlomiej.naprawiamy.utils.tryToRunFunctionOnInternet
import pl.sokolowskibartlomiej.naprawiamy.viewmodels.UserViewModel

class AccountFragment : BaseFragment() {

    private lateinit var mUserViewModel: UserViewModel
    private var mIsEditing = false
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var mCategoriesFragment: CategoriesFragment
    lateinit var loadingDialog: AlertDialog
    private lateinit var mUser: User
    private var mSpecialistCategoriesIds = arrayListOf<Int>()
    var isUserEdited = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_account, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        disableAllEditTexts()
        mBottomSheetBehavior = from(view.findViewById<View>(R.id.skillsSheet))
        mBottomSheetBehavior.state = STATE_HIDDEN

        mUserViewModel = ViewModelProvider(this@AccountFragment).get(UserViewModel::class.java)
        mUser = User.createUserFromString(PreferencesManager.getUserString() ?: "")
        loadingDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()
        if (mUser.specialist == true) {
            requireActivity().tryToRunFunctionOnInternet(
                { mUserViewModel.fetchSpecialistCategories() },
                {})
            view.showSkillsBtn.visibility = View.VISIBLE
        }

        mCategoriesFragment =
            childFragmentManager.fragments.first { it is CategoriesFragment } as CategoriesFragment
        mCategoriesFragment.setIsMultipleCategoriesAllowed(true)
        mCategoriesFragment.setIsCategoryClickable(false)

        setEditTextValues()

        mUserViewModel.categories.observe(viewLifecycleOwner, Observer { categories ->
            mSpecialistCategoriesIds = ArrayList(categories.map { it.id!! })
            mCategoriesFragment.setSelectedCategories(ArrayList(categories.map { it.id!! }))
        })

        view.accountFab.setOnClickListener {
            view.accountFab.setImageResource(if (mIsEditing) R.drawable.anim_save_to_edit else R.drawable.anim_edit_to_save)
            (view.accountFab.drawable as Animatable).start()
            mIsEditing = !mIsEditing
            if (mIsEditing) {
                enableAllEditTexts()
                if (mUser.specialist == true)
                    mCategoriesFragment.setIsCategoryClickable(true)
            } else {
                if (isUserEdited) {
                    val newUser = getNewUserValues()
                    if (newUser != null) {
                        loadingDialog.show()
                        requireActivity().tryToRunFunctionOnInternet({
                            mUserViewModel.updateUserInfo(newUser, this@AccountFragment)
                        }, { loadingDialog.dismiss() })
                    }
                }
                if (mUser.specialist == true) {
                    mCategoriesFragment.setIsCategoryClickable(false)
                    requireActivity().tryToRunFunctionOnInternet({
                        mUserViewModel.saveSpecialistCategories(
                            mSpecialistCategoriesIds,
                            mCategoriesFragment.getSelectedCategories()
                        )
                    }, {})
                    mSpecialistCategoriesIds = mCategoriesFragment.getSelectedCategories()
                }
                disableEditing()
            }
        }

        view.showSkillsBtn.setOnClickListener {
            view.accountFab.hide()
            mBottomSheetBehavior.state = STATE_EXPANDED
        }

        view.firstNameET.setOnTouchListener(mTouchListener)
        view.lastNameET.setOnTouchListener(mTouchListener)
        view.emailET.setOnTouchListener(mTouchListener)
        view.phoneET.setOnTouchListener(mTouchListener)
        view.addressET.setOnTouchListener(mTouchListener)
        view.showSkillsBtn.setOnTouchListener(mTouchListener)
    }

    private fun setEditTextValues() {
        view?.firstNameET?.setText(mUser.firstName ?: "")
        view?.lastNameET?.setText(mUser.lastName ?: "")
        view?.emailET?.setText(mUser.email ?: "")
        view?.phoneET?.setText(mUser.phoneNr ?: "")
        view?.addressET?.setText(mUser.address ?: "")
    }

    fun onBackPressed(): Boolean {
        if (::mBottomSheetBehavior.isInitialized && mBottomSheetBehavior.state == STATE_EXPANDED) {
            hideBottomSheet()
            return false
        }
        return true
    }

    fun disableEditing() {
        view?.accountFab?.setImageResource(R.drawable.anim_save_to_edit)
        (view?.accountFab?.drawable as Animatable).start()
        isUserEdited = false
        mIsEditing = false
        mCategoriesFragment.setSelectedCategories(mSpecialistCategoriesIds)
        disableAllEditTexts()
    }

    private fun getNewUserValues(): User? {
        val user = User(
            mUser.id,
            mUser.specialist,
            emailET.text.toString().trim(),
            firstNameET.text.toString().trim(),
            lastNameET.text.toString().trim(),
            phoneET.text.toString().trim(),
            mUser.passwordHash,
            addressET.text.toString().trim(),
            mUser.token
        )
        return if (mUser.getUserAsString() == user.getUserAsString()) null else user
    }

    fun onUpdateSuccessful() {
        Toast.makeText(
            requireContext(), requireContext().getText(R.string.data_saved), Toast.LENGTH_SHORT
        ).show()
        view?.accountFab?.setImageResource(R.drawable.anim_save_to_edit)
        (view?.accountFab?.drawable as Animatable).start()
    }

    fun showDataNotSavedDialog() =
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.update_error_title)
            .setMessage(R.string.update_error_message)
            .setCancelable(true)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()

    fun hideBottomSheet() {
        mBottomSheetBehavior.state = STATE_HIDDEN
        view?.accountFab?.show()
    }

    private fun enableAllEditTexts() {
        view?.firstNameET?.enable()
        view?.lastNameET?.enable()
        view?.emailET?.enable()
        view?.phoneET?.enable()
        view?.addressET?.enable()
    }

    private fun disableAllEditTexts() {
        view?.firstNameET?.apply {
            disable()
            clearFocus()
        }
        view?.lastNameET?.apply {
            disable()
            clearFocus()
        }
        view?.emailET?.apply {
            disable()
            clearFocus()
        }
        view?.phoneET?.apply {
            disable()
            clearFocus()
        }
        view?.addressET?.apply {
            disable()
            clearFocus()
        }
    }

    private val mTouchListener = View.OnTouchListener { _, _ ->
        isUserEdited = true
        false
    }
}
