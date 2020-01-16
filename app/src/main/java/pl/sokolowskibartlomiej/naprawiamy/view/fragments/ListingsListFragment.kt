package pl.sokolowskibartlomiej.naprawiamy.view.fragments

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_listings_list.view.*
import kotlinx.android.synthetic.main.dialog_rating.view.*
import kotlinx.android.synthetic.main.fragment_listings_list.view.*
import pl.sokolowskibartlomiej.naprawiamy.R
import pl.sokolowskibartlomiej.naprawiamy.utils.PreferencesManager
import pl.sokolowskibartlomiej.naprawiamy.utils.tryToRunFunctionOnInternet
import pl.sokolowskibartlomiej.naprawiamy.view.activities.MainActivity
import pl.sokolowskibartlomiej.naprawiamy.view.adapters.ListingsRecyclerAdapter
import pl.sokolowskibartlomiej.naprawiamy.viewmodels.ListingsListViewModel

class ListingsListFragment : BaseFragment() {

    private lateinit var mViewModel: ListingsListViewModel
    private lateinit var mAdapter: ListingsRecyclerAdapter
    private lateinit var mSearchView: SearchView
    lateinit var loadingDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_listings_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflateToolbarMenu(view.listingToolbar)
        setHasOptionsMenu(true)

        mAdapter = ListingsRecyclerAdapter(this@ListingsListFragment)
        view.listingsRecyclerView.apply {
            layoutManager = LinearLayoutManager(view.context)
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
        }
        view.listingsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!PreferencesManager.isSpecialist() && dy < 0 && !view.addListingBtn.isShown)
                    view.addListingBtn.show()
                else if (!PreferencesManager.isSpecialist() && dy > 0 && view.addListingBtn.isShown)
                    view.addListingBtn.hide()

                if (PreferencesManager.isSpecialist() && !recyclerView.canScrollVertically(1)) {
                    loadMoreListings()
                }
            }
        })

        mViewModel =
            ViewModelProvider(requireActivity() as MainActivity).get(ListingsListViewModel::class.java)
        fetchListings()

        mViewModel.listings.observe(viewLifecycleOwner, Observer { listings ->
            //            view.listingsRecyclerView.layoutAnimation =
//                    AnimationUtils.loadLayoutAnimation(view.listingsRecyclerView.context, R.anim.layout_animation_fall_down)
            mAdapter.setListingsList(listings)
//            view.listingsRecyclerView.scheduleLayoutAnimation()
            view.loadingIndicator.hide()
            view.listingsSwipeToRefresh.isRefreshing = false
            if (listings.isEmpty()) {
                view.emptyView.visibility = View.VISIBLE
            } else {
                view.emptyView.visibility = View.INVISIBLE
            }
        })

        mViewModel.listingVotes.observe(viewLifecycleOwner, Observer { listingVotes ->
            mAdapter.setListingVotes(listingVotes)
        })

        if (PreferencesManager.isSpecialist()) view.addListingBtn.hide()
        else view.addListingBtn.show()

        loadingDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()

        view.listingsSwipeToRefresh.apply {
            setOnRefreshListener { fetchListings() }
            setColorSchemeColors(
                ContextCompat.getColor(context, R.color.swipe_refresh_color_1),
                ContextCompat.getColor(context, R.color.swipe_refresh_color_2),
                ContextCompat.getColor(context, R.color.swipe_refresh_color_3),
                ContextCompat.getColor(context, R.color.swipe_refresh_color_4)
            )
        }

        view.addListingBtn.setOnClickListener {
            requireActivity().findNavController(R.id.navHostFragment)
                .navigate(ListingsListFragmentDirections.showAddListingFragment())
        }
    }

    fun onBackPressed(): Boolean {
        return if (!mSearchView.isIconified) {
            mSearchView.onActionViewCollapsed()
            false
        } else {
            true
        }
    }

    private fun fetchListings() {
        mSearchView.onActionViewCollapsed()
        if (!PreferencesManager.isSpecialist())
            requireActivity().tryToRunFunctionOnInternet({ mViewModel.fetchVotes() }, {})
        requireActivity().tryToRunFunctionOnInternet({ mViewModel.fetchListings() }, {})
    }

    private fun loadMoreListings() {
        if (PreferencesManager.isSpecialist() && mViewModel.numberOfAllListings > mViewModel.currentNumberOfListings) {
            loadingDialog.show()
            requireActivity().tryToRunFunctionOnInternet(
                { mViewModel.fetchMoreListings(this@ListingsListFragment) },
                { loadingDialog.dismiss() })
        }
    }

    private fun inflateToolbarMenu(toolbar: Toolbar) {
        toolbar.inflateMenu(R.menu.listings_menu)
        val searchManager =
            requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        mSearchView = toolbar.menu.findItem(R.id.action_search).actionView as SearchView
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        mSearchView.maxWidth = Integer.MAX_VALUE

        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                mAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                mAdapter.filter.filter(newText)
                return false
            }
        })
    }

    @SuppressLint("InflateParams")
    fun showRatingDialog(listingId: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rating, null)

        val dialog = AlertDialog.Builder(context!!)
            .setTitle(R.string.rate_realization_dialog_title)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.send), null)
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog?.dismiss() }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (dialogView.ratingBar.rating == 0f) {
                    Toast.makeText(
                        requireContext(),
                        R.string.rate_realization_dialog_error,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                } else {
                    loadingDialog.show()
                    requireActivity().tryToRunFunctionOnInternet({
                        mViewModel.sendVote(
                            listingId,
                            dialogView.ratingBar.rating.toInt(),
                            this@ListingsListFragment
                        )
                    }, { loadingDialog.dismiss() })
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    fun showDeleteVoteDialog(listingId: Int, rating: Int) =
        AlertDialog.Builder(context!!)
            .setTitle(R.string.delete_vote_dialog_title)
            .setMessage(String.format(getString(R.string.delete_vote_dialog_message), rating))
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                dialog.dismiss()
                loadingDialog.show()
                requireActivity().tryToRunFunctionOnInternet({
                    mViewModel.deleteVote(listingId, this@ListingsListFragment)
                }, { loadingDialog.dismiss() })
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog?.dismiss() }
            .create()
            .show()

    fun onVoteSaveSuccessful() {

        Toast.makeText(
            requireContext(),
            requireContext().getText(R.string.vote_saved),
            Toast.LENGTH_SHORT
        ).show()
        requireActivity().tryToRunFunctionOnInternet({ mViewModel.fetchVotes() }, {})
    }

    fun onVoteSaveFailed() {
        Toast.makeText(
            requireContext(),
            requireContext().getText(R.string.vote_save_error),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun onVoteDeleteSuccessful() {
        Toast.makeText(
            requireContext(),
            requireContext().getText(R.string.vote_deleted),
            Toast.LENGTH_SHORT
        ).show()
        requireActivity().tryToRunFunctionOnInternet({ mViewModel.fetchVotes() }, {})
    }

    fun onVoteDeleteFailed() {
        Toast.makeText(
            requireContext(),
            requireContext().getText(R.string.vote_delete_error),
            Toast.LENGTH_SHORT
        ).show()
    }
}
