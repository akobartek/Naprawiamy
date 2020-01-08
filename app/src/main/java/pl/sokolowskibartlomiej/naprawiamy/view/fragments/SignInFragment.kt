package pl.sokolowskibartlomiej.naprawiamy.view.fragments


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_sign_in.*
import pl.sokolowskibartlomiej.naprawiamy.R
import pl.sokolowskibartlomiej.naprawiamy.utils.PreferencesManager
import pl.sokolowskibartlomiej.naprawiamy.utils.tryToRunFunctionOnInternet
import pl.sokolowskibartlomiej.naprawiamy.view.activities.SignInActivity
import pl.sokolowskibartlomiej.naprawiamy.viewmodels.SignInViewModel

class SignInFragment : BaseFragment() {

    private lateinit var mSignInViewModel: SignInViewModel
    private lateinit var mLoadingDialog: AlertDialog
    private var mIsSigningIn = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_sign_in, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSignInViewModel = ViewModelProvider(this@SignInFragment).get(SignInViewModel::class.java)
        setOnClickListeners()

        mSignInViewModel.user.observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                if (mLoadingDialog.isShowing) mLoadingDialog.dismiss()
                PreferencesManager.setBearerToken(user.token!!)
                PreferencesManager.setUserString(user)
                Toast.makeText(requireActivity(), R.string.signed_in, Toast.LENGTH_SHORT).show()
                (requireActivity() as SignInActivity).returnActivity(true)
            } else {
                PreferencesManager.setBearerToken("")
                PreferencesManager.setUserString(null)
                showAccountProblemDialog()
            }
        })

        mLoadingDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()
    }

    override fun onStop() {
        if (mLoadingDialog.isShowing) mLoadingDialog.dismiss()
        super.onStop()
    }

    private fun setOnClickListeners() {
        signInBtn?.setOnClickListener {
            requireActivity().tryToRunFunctionOnInternet(
                { if (mIsSigningIn) signIn() else signUp() },
                {})
        }

        signUpBtn?.setOnClickListener {
            mIsSigningIn = !mIsSigningIn
            signInBtn.text = getString(if (!mIsSigningIn) R.string.sign_up else R.string.sign_in)
            signUpBtn.text =
                getString(if (!mIsSigningIn) R.string.back_to_sign_in else R.string.join_naprawiamy)
        }

        loginLayout.setOnClickListener {
            (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        }
        logo.setOnClickListener {
            (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        }
    }

    private fun signIn() {
        mLoadingDialog.show()
        val email = emailET.text.toString().trim()
        val password = passwordET.text.toString().trim()
        if (!areEmailAndPasswordValid(email, password)) {
            mLoadingDialog.dismiss()
            return
        }
        try {
            requireActivity().tryToRunFunctionOnInternet({
                mSignInViewModel.signInWithEmail(
                    arguments!!.getInt("accountType", 0) == 1,
                    email,
                    password,
                    this@SignInFragment
                )
            }, { mLoadingDialog.dismiss() })
        } catch (exc: Throwable) {
            showAccountProblemDialog()
        }
    }

    private fun signUp() {
        mLoadingDialog.show()
        val email = emailET.text.toString().trim()
        val password = passwordET.text.toString().trim()
        if (!areEmailAndPasswordValid(email, password)) {
            mLoadingDialog.dismiss()
            return
        }
        try {
            requireActivity().tryToRunFunctionOnInternet({
                mSignInViewModel.signUpWithEmail(
                    arguments!!.getInt("accountType", 0) == 1,
                    email,
                    password,
                    this@SignInFragment
                )
            }, { mLoadingDialog.dismiss() })
        } catch (exc: Throwable) {
            showAccountProblemDialog()
        }
    }

    private fun areEmailAndPasswordValid(email: String, password: String): Boolean {
        var isValid = true

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailET.error = getString(R.string.email_error_empty)
            isValid = false
        }
        if (password.isEmpty()) {
            passwordET.error = getString(R.string.password_error_empty)
            isValid = false
        }
        return isValid
    }

    fun showAccountProblemDialog() =
        AlertDialog.Builder(requireContext())
            .setMessage(if (mIsSigningIn) R.string.account_not_found_dialog_message else R.string.account_exists_dialog_message)
            .setCancelable(true)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                if (mLoadingDialog.isShowing) mLoadingDialog.dismiss()
                dialog.dismiss()
            }
            .create()
            .show()

    companion object {
        fun newInstance(accountType: Int): SignInFragment {
            return SignInFragment().apply {
                arguments = Bundle().apply {
                    putInt("accountType", accountType)
                }
            }
        }
    }
}
