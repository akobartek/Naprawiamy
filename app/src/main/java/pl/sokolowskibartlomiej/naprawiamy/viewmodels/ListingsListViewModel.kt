package pl.sokolowskibartlomiej.naprawiamy.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.sokolowskibartlomiej.naprawiamy.apicalls.NaprawiamyApiRepository
import pl.sokolowskibartlomiej.naprawiamy.model.ListingVote
import pl.sokolowskibartlomiej.naprawiamy.model.ListingWithImages
import pl.sokolowskibartlomiej.naprawiamy.utils.PreferencesManager
import pl.sokolowskibartlomiej.naprawiamy.view.fragments.ListingsListFragment

class ListingsListViewModel(val app: Application) : AndroidViewModel(app) {

    private val repository = NaprawiamyApiRepository()

    val listings = MutableLiveData<MutableList<ListingWithImages>>()

    val listingVotes = MutableLiveData<MutableList<ListingVote>>()

    var currentNumberOfListings = 0
    var numberOfAllListings = 0

    fun fetchListings() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list =
                    if (PreferencesManager.isSpecialist()) {
                        numberOfAllListings = repository.getOpenListings()
                        if (numberOfAllListings == 0) {
                            listings.postValue(mutableListOf())
                            return@launch
                        }
                        val listingsToLoad = when {
                            numberOfAllListings <= 7 -> numberOfAllListings
                            currentNumberOfListings != 0 -> currentNumberOfListings
                            else -> 7
                        }
                        repository.getOpenListings(0, listingsToLoad)
                    } else repository.getMyListings()
                val result = ArrayList(list.map { ListingWithImages(it) })
                list.forEach { listing ->
                    repository.getListingImages(listing.id!!).forEach { listingImage ->
                        val index = result.indexOfFirst { listing.id == listingImage.listingId }
                        val image = repository.getImage(listingImage.imageId)
                        val newImagesValue = result[index]?.images + " ${image.url}"
                        result[index].images = newImagesValue.trim()
                    }
                }

                currentNumberOfListings = result.size
                listings.postValue(result.toMutableList())
            } catch (exc: Throwable) {
                Log.e("ListingsListViewModel", exc.toString())
                listings.postValue(mutableListOf())
            }
        }
    }

    fun fetchMoreListings(fragment: ListingsListFragment) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newListings = repository.getOpenListings(currentNumberOfListings, 7)
                val result = ArrayList(newListings.map { ListingWithImages(it) })
                newListings.forEach { listing ->
                    repository.getListingImages(listing.id!!).forEach { listingImage ->
                        val index = result.indexOfFirst { listing.id == listingImage.listingId }
                        val image = repository.getImage(listingImage.imageId)
                        val newImagesValue = result[index]?.images + " ${image.url}"
                        result[index].images = newImagesValue.trim()
                    }
                }

                val newList = listings.value
                newList?.addAll(result)
                currentNumberOfListings = newList?.size ?: 0
                listings.postValue(newList)
                fragment.requireActivity().runOnUiThread { fragment.loadingDialog.dismiss() }
            } catch (exc: Throwable) {
                Log.e("ListingsListViewModel", exc.toString())
                fragment.requireActivity().runOnUiThread { fragment.loadingDialog.dismiss() }
            }
        }
    }

    fun fetchVotes() {
        if (!PreferencesManager.isSpecialist())
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val numberOfVotes = repository.getListingVotes()
                    listingVotes.postValue(
                        repository.getListingVotes(0, numberOfVotes).toMutableList()
                    )
                } catch (exc: Throwable) {
                    Log.e("ListingsListViewModel", exc.toString())
                }
            }
    }

    fun deleteVote(listingId: Int, fragment: ListingsListFragment) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (repository.deleteListingVote(listingId))
                    fragment.requireActivity().runOnUiThread {
                        fragment.loadingDialog.dismiss()
                        fragment.onVoteDeleteSuccessful()
                    }
                else
                    fragment.requireActivity().runOnUiThread {
                        fragment.loadingDialog.dismiss()
                        fragment.onVoteDeleteFailed()
                    }
            } catch (exc: Throwable) {
                Log.e("ListingsListViewModel", exc.toString())
                fragment.requireActivity().runOnUiThread {
                    fragment.loadingDialog.dismiss()
                    fragment.onVoteDeleteFailed()
                }
            }
        }
    }

    fun sendVote(listingId: Int, rating: Int, fragment: ListingsListFragment) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.addListingVote(listingId, rating)
                fragment.requireActivity().runOnUiThread {
                    fragment.loadingDialog.dismiss()
                    fragment.onVoteSaveSuccessful()
                }
            } catch (exc: Throwable) {
                Log.e("ListingDetailsViewModel", exc.message.toString())
                fragment.requireActivity().runOnUiThread {
                    fragment.loadingDialog.dismiss()
                    fragment.onVoteSaveFailed()
                }
            }
        }
    }
}