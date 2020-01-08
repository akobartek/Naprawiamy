package pl.sokolowskibartlomiej.naprawiamy.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_categories.view.*
import pl.sokolowskibartlomiej.naprawiamy.R
import pl.sokolowskibartlomiej.naprawiamy.model.Category
import pl.sokolowskibartlomiej.naprawiamy.utils.tryToRunFunctionOnInternet
import pl.sokolowskibartlomiej.naprawiamy.view.adapters.SkillSpanSizeLookup
import pl.sokolowskibartlomiej.naprawiamy.view.adapters.SkillsRecyclerAdapter
import pl.sokolowskibartlomiej.naprawiamy.viewmodels.CategoriesViewModel

class CategoriesFragment : BaseFragment() {

    private lateinit var mViewModel: CategoriesViewModel
    private lateinit var mAdapter: SkillsRecyclerAdapter
    private var selectedCategories = arrayListOf<Int>()
    private var isCategoryClickable = true
    private var isMultipleAllowed = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_categories, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel =
            ViewModelProvider(this@CategoriesFragment).get(CategoriesViewModel::class.java)
        requireActivity().tryToRunFunctionOnInternet({ mViewModel.fetchCategories() }, {})

        mAdapter = SkillsRecyclerAdapter()
        view.collapseArrow.setOnClickListener {
            when (parentFragment) {
                is AccountFragment -> (parentFragment as AccountFragment).hideBottomSheet()
                is ListingAddFragment -> (parentFragment as ListingAddFragment).hideBottomSheet()
                is ListingDetailsFragment -> (parentFragment as ListingDetailsFragment).hideBottomSheet()
            }
        }
        view.skillsRecyclerView.apply {
            adapter = mAdapter
            setHasFixedSize(true)
            (layoutManager as GridLayoutManager).spanSizeLookup =
                SkillSpanSizeLookup(mAdapter)
        }

        mViewModel.categories.observe(viewLifecycleOwner, Observer { categories ->
            if (!categories.isNullOrEmpty()) {
                val categoriesSorted = arrayListOf<Category>()
                categories.filter { it.parentCategoryId == null }
                    .forEach { parentCategory ->
                        categoriesSorted.add(parentCategory)
                        categories.filter { it.parentCategoryId == parentCategory.id }
                            .forEach { childCategory ->
                                categoriesSorted.add(childCategory)
                            }
                    }
                mAdapter.submitSkillsList(categoriesSorted)
                mAdapter.setIsClickable(isCategoryClickable)
                mAdapter.setIsMultipleCategoriesAllowed(isMultipleAllowed)
                if (isMultipleAllowed)
                    mAdapter.setSelectedCategories(selectedCategories)
                else
                    mAdapter.setSelectedCategoryId(selectedCategories[0])
            }
        })
    }

    fun setIsCategoryClickable(isClickable: Boolean) {
        if (::mAdapter.isInitialized)
            mAdapter.setIsClickable(isClickable)
        isCategoryClickable = isClickable
    }

    fun setIsMultipleCategoriesAllowed(isAllowed: Boolean) {
        if (::mAdapter.isInitialized)
            mAdapter.setIsMultipleCategoriesAllowed(isMultipleAllowed)
        isMultipleAllowed = isAllowed
    }

    fun getSelectedCategoryId(): Int? = mAdapter.getSelectedCategoryId()
    fun setSelectedCategoryId(categoryId: Int) {
        if (::mAdapter.isInitialized)
            mAdapter.setSelectedCategoryId(categoryId)
        selectedCategories = arrayListOf(categoryId)
    }

    fun getSelectedCategories() = mAdapter.getSelectedCategories()
    fun setSelectedCategories(categoriesIds: ArrayList<Int>) {
        if (::mAdapter.isInitialized)
            mAdapter.setSelectedCategories(categoriesIds)
        selectedCategories = categoriesIds
    }
}
