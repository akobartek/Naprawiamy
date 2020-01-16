package pl.sokolowskibartlomiej.naprawiamy.view.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
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
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.transition.TransitionInflater
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.content_listing_details.view.*
import kotlinx.android.synthetic.main.fragment_listing_details.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.sokolowskibartlomiej.naprawiamy.R
import pl.sokolowskibartlomiej.naprawiamy.apicalls.RetrofitClient.BASE_API_URL
import pl.sokolowskibartlomiej.naprawiamy.model.Listing
import pl.sokolowskibartlomiej.naprawiamy.model.ListingProposal
import pl.sokolowskibartlomiej.naprawiamy.model.ListingWithImages
import pl.sokolowskibartlomiej.naprawiamy.utils.*
import pl.sokolowskibartlomiej.naprawiamy.view.adapters.PhotoDetailsRecyclerAdapter
import pl.sokolowskibartlomiej.naprawiamy.view.adapters.ProposalsRecyclerAdapter
import pl.sokolowskibartlomiej.naprawiamy.view.views.CircleIndicatorDecoration
import pl.sokolowskibartlomiej.naprawiamy.viewmodels.ListingDetailsViewModel
import java.text.SimpleDateFormat
import java.util.*


class ListingDetailsFragment : BaseFragment() {

    private lateinit var mViewModel: ListingDetailsViewModel
    private lateinit var mPhotosAdapter: PhotoDetailsRecyclerAdapter
    private lateinit var mProposalsAdapter: ProposalsRecyclerAdapter
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var mCategoriesFragment: CategoriesFragment
    lateinit var loadingDialog: AlertDialog
    private var mListing: ListingWithImages? = null
    private var mListingDeadline = Date()
    private var specialistProposal: ListingProposal? = null
    var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(context)
                .inflateTransition(R.transition.image_shared_element_transition)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_listing_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.listingDetailsToolbar.setNavigationIcon(R.drawable.ic_arrow_back)

        mViewModel =
            ViewModelProvider(this@ListingDetailsFragment).get(ListingDetailsViewModel::class.java)
        mListing = requireArguments().getParcelable("listing")
        mProposalsAdapter = ProposalsRecyclerAdapter(
            this@ListingDetailsFragment,
            mListing!!.listing.acceptedProposalId != null
        )
        if (!PreferencesManager.isSpecialist())
            requireActivity().tryToRunFunctionOnInternet(
                { mViewModel.fetchProposals(mListing!!.listing.id!!) },
                {})
        else {
            view.updateListingBtn.setImageDrawable(requireContext().getDrawable(R.drawable.ic_accept))
            requireActivity().tryToRunFunctionOnInternet(
                { mViewModel.fetchSpecialistProposals() },
                {})
        }

        if (!mListing?.images.isNullOrEmpty()) {
            GlideApp.with(requireContext())
                .load(
                    BASE_API_URL.substring(0, BASE_API_URL.length - 1) +
                            mListing!!.images.split(" ")[0].split("~")[1]
                )
                .placeholder(R.drawable.ic_no_photo)
                .into(view.listingDetailsPhoto)
        }
        view.listingDetailsPhoto.transitionName = requireArguments().getString("transitionName")
        mPhotosAdapter =
            PhotoDetailsRecyclerAdapter(!PreferencesManager.isSpecialist()) { hideEmptyList() }
        view.photosRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(
                CircleIndicatorDecoration(
                    requireContext().getAttributeColor(R.attr.colorPrimaryDark),
                    requireContext().getAttributeColor(R.attr.colorIndicatorInactive)
                )
            )
            adapter = mPhotosAdapter
        }
        PagerSnapHelper().attachToRecyclerView(view.photosRecyclerView)
        if (!mListing?.images.isNullOrEmpty()) {
            GlobalScope.launch(Dispatchers.IO) {
                delay(450)
                requireActivity().runOnUiThread {
                    view.photosRecyclerView?.visibility = View.VISIBLE
                    view.listingDetailsPhoto?.setImageResource(android.R.color.transparent)
                }
            }
            mPhotosAdapter.setPhotosList(mListing!!.images.split(" "))
        } else {
            view.photosRecyclerView?.visibility = View.GONE
        }

        view.listingDetailsToolbar.title = mListing!!.listing.title
        view.titleET.setText(mListing!!.listing.title)
        view.descriptionET.setText(mListing!!.listing.description)
        view.addressET.setText(mListing!!.listing.address)
        view.proposedValueET.setText(mListing!!.listing.proposedValue.toString())
        view.deadlineET.setText(mListing!!.listing.maxDeadline?.format() ?: "")
        mListingDeadline = mListing!!.listing.maxDeadline ?: Date()
        mBottomSheetBehavior = BottomSheetBehavior.from(view.findViewById<View>(R.id.skillsSheet))
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        mCategoriesFragment =
            childFragmentManager.fragments.first { it is CategoriesFragment } as CategoriesFragment
        mCategoriesFragment.setIsMultipleCategoriesAllowed(false)
        if (mListing?.listing?.categoryId != null)
            mCategoriesFragment.setSelectedCategoryId(mListing!!.listing.categoryId!!)
        if (PreferencesManager.isSpecialist())
            mCategoriesFragment.setIsCategoryClickable(false)

        view.proposalsRecyclerView.apply {
            layoutManager = LinearLayoutManager(view.context)
            itemAnimator = DefaultItemAnimator()
            adapter = mProposalsAdapter
        }

        loadingDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()

        mViewModel.proposals.observe(viewLifecycleOwner, Observer { proposals ->
            if (!PreferencesManager.isSpecialist())
                if (proposals.isNullOrEmpty()) {
                    view.proposalsText.visibility = View.GONE
                    view.proposalsRecyclerView.visibility = View.INVISIBLE
                } else {
                    view.proposalsText.visibility = View.VISIBLE
                    view.proposalsRecyclerView.visibility = View.VISIBLE
                    val list =
                        if (mListing!!.listing.acceptedProposalId == null) proposals
                        else proposals.filter { it.id == mListing!!.listing.acceptedProposalId }
                    mProposalsAdapter.setProposalsList(list)
                    list.forEach { mViewModel.fetchSpecialists(it.id!!) }
                }
            else
                if (proposals.any { it.listingId == mListing!!.listing.id })
                    specialistProposal = proposals.first { it.listingId == mListing!!.listing.id }
        })

        mViewModel.specialists.observe(viewLifecycleOwner, Observer { specialists ->
            mProposalsAdapter.setSpecialistsList(specialists)
        })

        view.listingDetailsToolbar.setNavigationOnClickListener {
            view.findNavController().navigateUp()
        }

        view.showSkillsBtn.setOnClickListener {
            view.updateListingBtn.hide()
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        if (!PreferencesManager.isSpecialist()) {
            view.listingDetailsPhoto.setOnTouchListener(mTouchListener)
            view.titleET.setOnTouchListener(mTouchListener)
            view.descriptionET.setOnTouchListener(mTouchListener)
            view.addressET.setOnTouchListener(mTouchListener)
            view.proposedValueET.setOnTouchListener(mTouchListener)
            view.deadlineET.setOnTouchListener(mTouchListener)

            view.deadlineET.setOnClickListener {
                (it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)

                val calendar = Calendar.getInstance()
                calendar.time = mListingDeadline

                DatePickerDialog(
                    it.context,
                    getDateListener(view),
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        } else {
            view.titleET.disable()
            view.descriptionET.disable()
            view.addressET.disable()
            view.proposedValueET.disable()
            view.deadlineET.disable()
        }

        view.updateListingBtn.setOnClickListener {
            if (PreferencesManager.isSpecialist()) {
                if (specialistProposal != null) showProposalAlreadyExistsDialog()
                else showProposalDialog()
            } else {
                // TODO() -> Delete photos
                if (view.titleET.text.toString() == "" || view.descriptionET.text.toString() == "" ||
                    view.addressET.text.toString() == "" || view.proposedValueET.text.toString() == ""
                ) {
                    Toast.makeText(
                        requireContext(),
                        requireContext().getText(R.string.empty_data_not_allowed),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (!isEditing || mListing == null)
                    view.findNavController().navigateUp()
                else {
                    val updatedListing = Listing(
                        mListing!!.listing.id,
                        mListing!!.listing.clientId,
                        mCategoriesFragment.getSelectedCategoryId(),
                        view.titleET.text.toString().trim(),
                        view.descriptionET.text.toString().trim(),
                        view.addressET.text.toString().trim(),
                        view.proposedValueET.text.toString().toDouble(),
                        mListingDeadline,
                        mListing!!.listing.acceptedProposalId
                    )
                    if (mListing.toString() != updatedListing.toString()) {
                        loadingDialog.show()
                        requireActivity().tryToRunFunctionOnInternet({
                            mViewModel.updateListing(updatedListing, this@ListingDetailsFragment)
                        }, { loadingDialog.dismiss() })
                    }
                }
            }

        }
    }

    fun acceptProposal(proposalId: Int) {
        loadingDialog.show()
        requireActivity().tryToRunFunctionOnInternet({
            mViewModel.acceptProposal(proposalId, this@ListingDetailsFragment)
        }, { loadingDialog.dismiss() })
    }

    fun onBackPressed(): Boolean {
        if (::mBottomSheetBehavior.isInitialized && mBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            hideBottomSheet()
            return false
        }
        return true
    }

    fun hideBottomSheet() {
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        view?.updateListingBtn?.show()
    }

    private fun hideEmptyList() {
        view?.photosRecyclerView?.visibility = View.INVISIBLE
        view?.listingDetailsPhoto?.setImageResource(R.drawable.ic_no_photo)
    }

    fun onUpdateSuccessful() {
        Toast.makeText(
            requireContext(), requireContext().getText(R.string.data_saved), Toast.LENGTH_SHORT
        ).show()
        view?.findNavController()?.navigateUp()
    }

    fun onAcceptSuccessful() {
        Toast.makeText(
            requireContext(),
            requireContext().getText(R.string.proposal_accepted),
            Toast.LENGTH_SHORT
        ).show()
        view?.findNavController()?.navigateUp()
    }

    fun onProposalSaveSuccessful() {
        Toast.makeText(
            requireContext(),
            requireContext().getText(R.string.proposal_sent),
            Toast.LENGTH_SHORT
        ).show()
        view?.findNavController()?.navigateUp()
    }

    @SuppressLint("InflateParams")
    private fun showProposalDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_proposal, null)

        val dialog = AlertDialog.Builder(context!!)
            .setTitle(R.string.proposal_dialog_title)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.send), null)
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog?.dismiss() }
            .create()

        dialog.setOnShowListener {
            dialogView.deadlineET.setText(mListingDeadline.format())
            dialogView.deadlineET.setOnClickListener {
                (it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)

                val calendar = Calendar.getInstance()
                calendar.time = mListingDeadline

                DatePickerDialog(
                    it.context,
                    getDateListener(dialogView),
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val proposedValue = dialogView.proposedValueET.text.toString().trim()
                if (proposedValue.isEmpty()) {
                    dialogView.proposedValueET.error =
                        getString(R.string.proposed_value_error_empty)
                    return@setOnClickListener
                } else {
                    loadingDialog.show()
                    requireActivity().tryToRunFunctionOnInternet({
                        mViewModel.sendProposal(
                            ListingProposal(
                                null,
                                mListing!!.listing.id!!,
                                PreferencesManager.getUserString()?.split("~")?.get(0)?.toInt()!!,
                                proposedValue.toDouble(),
                                mListingDeadline
                            ), this@ListingDetailsFragment
                        )
                    }, { dialog.dismiss() })
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
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

    fun showProposalAlreadyExistsDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage(
                String.format(
                    getString(R.string.proposal_error),
                    specialistProposal!!.offeredValue
                )
            )
            .setCancelable(true)
            .setPositiveButton(R.string.delete) { dialog, _ ->
                loadingDialog.show()
                requireActivity().tryToRunFunctionOnInternet({
                    mViewModel.deleteProposal(
                        mListing!!.listing.id!!,
                        this@ListingDetailsFragment
                    )
                }, { dialog.dismiss() })
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    fun onProposalDeleteSuccessful() {
        specialistProposal = null
        Toast.makeText(
            requireContext(),
            requireContext().getText(R.string.proposal_deleted),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun onProposalDeleteFailed() {
        Toast.makeText(
            requireContext(),
            requireContext().getText(R.string.proposal_delete_error),
            Toast.LENGTH_SHORT
        ).show()
    }

    private val mTouchListener = View.OnTouchListener { _, _ ->
        isEditing = true
        false
    }

    private fun getDateListener(dateView: View): DatePickerDialog.OnDateSetListener {
        return DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val dateString =
                StringBuilder().append(day).append(".").append(month + 1).append(".").append(year)
                    .toString()
            mListingDeadline =
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(dateString)!!
            dateView.deadlineET?.setText(mListingDeadline.format())
        }
    }
}
