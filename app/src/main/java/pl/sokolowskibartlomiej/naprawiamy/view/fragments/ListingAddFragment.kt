package pl.sokolowskibartlomiej.naprawiamy.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.content_listing_add.view.*
import kotlinx.android.synthetic.main.fragment_listing_add.view.*
import pl.sokolowskibartlomiej.naprawiamy.R
import pl.sokolowskibartlomiej.naprawiamy.model.Listing
import pl.sokolowskibartlomiej.naprawiamy.utils.PreferencesManager
import pl.sokolowskibartlomiej.naprawiamy.utils.format
import pl.sokolowskibartlomiej.naprawiamy.utils.getAttributeColor
import pl.sokolowskibartlomiej.naprawiamy.utils.tryToRunFunctionOnInternet
import pl.sokolowskibartlomiej.naprawiamy.view.activities.MainActivity
import pl.sokolowskibartlomiej.naprawiamy.view.adapters.PhotoRecyclerAdapter
import pl.sokolowskibartlomiej.naprawiamy.view.views.CircleIndicatorDecoration
import pl.sokolowskibartlomiej.naprawiamy.viewmodels.ListingAddViewModel
import java.text.SimpleDateFormat
import java.util.*


class ListingAddFragment : BaseFragment() {

    companion object {
        const val PICK_IMAGE_MULTIPLE = 2137
        const val REQUEST_CODE_PERMISSIONS = 911
    }

    private lateinit var mViewModel: ListingAddViewModel
    private lateinit var mAdapter: PhotoRecyclerAdapter
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var mCategoriesFragment: CategoriesFragment
    lateinit var loadingDialog: AlertDialog
    private var mListingDeadline = Date()
    var isListingEdited = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_listing_add, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.addListingToolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        view.addListingToolbar.setNavigationOnClickListener {
            view.findNavController().navigateUp()
        }

        mViewModel =
            ViewModelProvider(requireActivity() as MainActivity).get(ListingAddViewModel::class.java)

        mAdapter = PhotoRecyclerAdapter { hideEmptyList() }
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
            adapter = mAdapter
        }
        PagerSnapHelper().attachToRecyclerView(view.photosRecyclerView)
        view.deadlineET.setText(mListingDeadline.format())

        mBottomSheetBehavior = BottomSheetBehavior.from(view.findViewById<View>(R.id.skillsSheet))
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        mCategoriesFragment =
            childFragmentManager.fragments.first { it is CategoriesFragment } as CategoriesFragment
        mCategoriesFragment.setIsMultipleCategoriesAllowed(false)
        mCategoriesFragment.setSelectedCategoryId(0)

        loadingDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()

        view.addPhotosBtn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                askForPermission()
            } else {
                choosePhotos()
            }
        }

        view.deadlineET.setOnClickListener {
            (it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)

            val calendar = Calendar.getInstance()
            calendar.time = mListingDeadline

            DatePickerDialog(
                it.context,
                myBeginDateListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        view.saveNewListingBtn.setOnClickListener {
            if (view.titleET.text.toString() == "" || view.descriptionET.text.toString() == "" ||
                view.addressET.text.toString() == "" || view.proposedValueET.text.toString() == "" ||
                mCategoriesFragment.getSelectedCategoryId() == null
            ) {
                Toast.makeText(
                    requireContext(),
                    requireContext().getText(R.string.empty_data_not_allowed),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            loadingDialog.show()
            requireActivity().tryToRunFunctionOnInternet({
                mViewModel.saveListing(
                    Listing(
                        null,
                        PreferencesManager.getUserString()?.split("~")?.get(0)?.toInt(),
                        mCategoriesFragment.getSelectedCategoryId(),
                        view.titleET.text.toString(),
                        view.descriptionET.text.toString().trim(),
                        view.addressET.text.toString().trim(),
                        view.proposedValueET.text.toString().trim().toDouble(),
                        mListingDeadline,
                        null
                    ),
                    mAdapter.getPhotosList(),
                    this@ListingAddFragment
                )
            }, { loadingDialog.dismiss() })
        }

        view.showSkillsBtn.setOnClickListener {
            view.saveNewListingBtn.hide()
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        view.titleET.setOnTouchListener(mTouchListener)
        view.descriptionET.setOnTouchListener(mTouchListener)
        view.addressET.setOnTouchListener(mTouchListener)
        view.proposedValueET.setOnTouchListener(mTouchListener)
        view.deadlineET.setOnTouchListener(mTouchListener)
    }

    @SuppressLint("Recycle")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == Activity.RESULT_OK) {
            val photos = arrayListOf<Uri>()
            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    photos.add(data.clipData!!.getItemAt(i).uri)
                }
            } else if (data?.data != null) {
                photos.add(data.data!!)
            }
            mAdapter.setPhotosList(photos)
            view?.photosRecyclerView?.visibility = View.VISIBLE
            view?.addPhotosBtn?.setImageResource(android.R.color.transparent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                choosePhotos()
            } else {
                Toast.makeText(requireContext(), "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    fun onBackPressed(): Boolean {
        if (::mBottomSheetBehavior.isInitialized && mBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            hideBottomSheet()
            return false
        }
        return true
    }

    private fun askForPermission() {
        if ((ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) + ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
            )) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) ||
                ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                Snackbar.make(
                    requireView().findViewById(android.R.id.content),
                    "Please grant permissions to get data in sdcard",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(
                    "ENABLE"
                ) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        REQUEST_CODE_PERMISSIONS
                    )
                }.show()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    REQUEST_CODE_PERMISSIONS
                )
            }
        } else {
            choosePhotos()
        }
    }

    private fun choosePhotos() {
        Toast.makeText(requireContext(), R.string.select_pictures, Toast.LENGTH_SHORT).show()
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(
            Intent.createChooser(
                intent,
                requireContext().getString(R.string.select_gallery)
            ), PICK_IMAGE_MULTIPLE
        )
    }

    fun onSaveSuccessful() {
        Toast.makeText(
            requireContext(), requireContext().getText(R.string.data_saved), Toast.LENGTH_SHORT
        ).show()
        view?.findNavController()?.navigateUp()
    }

    fun hideBottomSheet() {
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        view?.saveNewListingBtn?.show()
    }

    private fun hideEmptyList() {
        view?.photosRecyclerView?.visibility = View.INVISIBLE
        view?.addPhotosBtn?.setImageResource(R.drawable.ic_no_photo)
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

    private val mTouchListener = View.OnTouchListener { _, _ ->
        isListingEdited = true
        false
    }
    private val myBeginDateListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
        val dateString =
            StringBuilder().append(day).append(".").append(month + 1).append(".").append(year)
                .toString()
        mListingDeadline = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(dateString)!!
        view?.deadlineET?.setText(mListingDeadline.format())
    }
}
