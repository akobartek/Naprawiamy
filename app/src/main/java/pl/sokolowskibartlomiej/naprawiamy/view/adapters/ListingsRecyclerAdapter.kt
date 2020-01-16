package pl.sokolowskibartlomiej.naprawiamy.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_listing.view.*
import pl.sokolowskibartlomiej.naprawiamy.R
import pl.sokolowskibartlomiej.naprawiamy.apicalls.RetrofitClient.BASE_API_URL
import pl.sokolowskibartlomiej.naprawiamy.model.ListingVote
import pl.sokolowskibartlomiej.naprawiamy.model.ListingWithImages
import pl.sokolowskibartlomiej.naprawiamy.utils.GlideApp
import pl.sokolowskibartlomiej.naprawiamy.utils.PreferencesManager
import pl.sokolowskibartlomiej.naprawiamy.view.fragments.ListingsListFragment
import java.util.*
import kotlin.collections.ArrayList

class ListingsRecyclerAdapter(val fragment: ListingsListFragment) :
    RecyclerView.Adapter<ListingsRecyclerAdapter.ListingViewHolder>(), Filterable {

    private var mListings = listOf<ListingWithImages>()
    private var mListingsFiltered = listOf<ListingWithImages>()
    private var mListingVotes = listOf<ListingVote>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder =
        ListingViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_listing,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) =
        holder.bindView(mListingsFiltered[position], position)

    override fun getItemCount(): Int = mListingsFiltered.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                mListingsFiltered = if (charString.isEmpty()) {
                    mListings
                } else {
                    val filteredList = ArrayList<ListingWithImages>()
                    for (listingIndex in mListings.indices) {
                        if (mListings[listingIndex].listing.title!!.toLowerCase(Locale.ROOT)
                                .contains(charString.toLowerCase(Locale.ROOT))
                        )
                            filteredList.add(mListings[listingIndex])
                    }

                    filteredList
                }

                val filterResults = FilterResults()
                filterResults.values = mListingsFiltered
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                @Suppress("UNCHECKED_CAST")
                mListingsFiltered = filterResults.values as List<ListingWithImages>
                notifyDataSetChanged()
            }
        }
    }

    fun setListingsList(list: List<ListingWithImages>) {
        mListings = list
        mListingsFiltered = list
        notifyDataSetChanged()
    }

    fun setListingVotes(list: List<ListingVote>) {
        mListingVotes = list
        notifyDataSetChanged()
    }


    inner class ListingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bindView(listingWithImages: ListingWithImages, listingPosition: Int) {
            val listing = listingWithImages.listing
            itemView.listingTitle.text = listing.title?.trim()
            itemView.listingCity.text = listing.address?.trim()
            itemView.listingPrice.visibility = View.VISIBLE
            itemView.listingPrice.text = "${listing.proposedValue?.toInt() ?: 0} zł"

            if (listingWithImages.images.isNotEmpty()) {
                GlideApp.with(itemView.context)
                    .load(
                        BASE_API_URL.substring(0, BASE_API_URL.length - 1) +
                                listingWithImages.images.split(" ")[0].split("~")[1]
                    )
                    .placeholder(R.drawable.ic_no_photo)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(itemView.listingPhoto)
            } else {
                itemView.listingPhoto.setImageResource(R.drawable.ic_no_photo)
            }

            ViewCompat.setTransitionName(itemView.listingPhoto, "listing$listingPosition")
            itemView.setOnClickListener {
                val bundle =
                    bundleOf(
                        "transitionName" to "listing$listingPosition",
                        "listing" to listingWithImages
                    )
                fragment.findNavController().navigate(
                    R.id.showListingDetailsFragment, bundle, null,
                    FragmentNavigatorExtras(itemView.listingPhoto to "listing$listingPosition")
                )
            }

            if (mListingVotes.any { it.listingProposalId == listing.acceptedProposalId }) {
                itemView.deleteVoteBtn.visibility = View.VISIBLE
                itemView.deleteVoteBtn.setOnClickListener {
                    fragment.showDeleteVoteDialog(
                        listing.id!!,
                        mListingVotes.first { it.listingProposalId == listing.acceptedProposalId }.rating
                    )
                }
                itemView.addRatingBtn.visibility = View.GONE
            } else if (listing.acceptedProposalId != null && !PreferencesManager.isSpecialist()) {
                itemView.addRatingBtn.visibility = View.VISIBLE
                itemView.addRatingBtn.setOnClickListener {
                    fragment.showRatingDialog(listing.id!!)
                }
                itemView.deleteVoteBtn.visibility = View.GONE
            } else {
                itemView.addRatingBtn.visibility = View.GONE
                itemView.deleteVoteBtn.visibility = View.GONE
            }
        }
    }
}