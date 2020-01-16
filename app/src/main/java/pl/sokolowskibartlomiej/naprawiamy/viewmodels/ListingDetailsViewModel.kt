package pl.sokolowskibartlomiej.naprawiamy.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.sokolowskibartlomiej.naprawiamy.apicalls.NaprawiamyApiRepository
import pl.sokolowskibartlomiej.naprawiamy.model.Listing
import pl.sokolowskibartlomiej.naprawiamy.model.ListingProposal
import pl.sokolowskibartlomiej.naprawiamy.model.ListingVote
import pl.sokolowskibartlomiej.naprawiamy.model.UserWithVotes
import pl.sokolowskibartlomiej.naprawiamy.view.fragments.ListingDetailsFragment

class ListingDetailsViewModel(val app: Application) : AndroidViewModel(app) {

    private val repository = NaprawiamyApiRepository()

    val proposals = MutableLiveData<MutableList<ListingProposal>>()
    val specialists = MutableLiveData<MutableList<UserWithVotes>>()

    init {
        specialists.postValue(mutableListOf())
    }

    fun fetchProposals(listingId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                proposals.postValue(repository.getListingProposalForListing(listingId).toMutableList())
            } catch (exc: Throwable) {
                proposals.postValue(mutableListOf())
            }
        }
    }

    fun fetchSpecialistProposals() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val numberOfProposals = repository.getListingProposals()
                if (numberOfProposals == 0) {
                    proposals.postValue(mutableListOf())
                    return@launch
                }
                proposals.postValue(
                    repository.getListingProposals(0, numberOfProposals).toMutableList()
                )
            } catch (exc: Throwable) {
                proposals.postValue(mutableListOf())
            }
        }
    }

    fun fetchSpecialists(listingProposalId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val specialist = repository.getSpecialistInfoByProposal(listingProposalId)
                val numberOfVotes = repository.getUserListingVotes(specialist.id!!)
                val votes =
                    if (numberOfVotes == 0) listOf()
                    else repository.getUserListingVotes(specialist.id, 0, numberOfVotes)
                specialists.value!!.add(UserWithVotes(specialist, votes))
                specialists.postValue(specialists.value)
            } catch (exc: Throwable) {
                Log.e("ListingDetailsViewModel", exc.message.toString())
            }
        }
    }

    fun updateListing(listing: Listing, fragment: ListingDetailsFragment) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isUpdateSuccessful = repository.updateListing(listing)
                fragment.requireActivity().runOnUiThread {
                    fragment.loadingDialog.dismiss()
                    if (isUpdateSuccessful) {
                        fragment.onUpdateSuccessful()
                    } else {
                        fragment.showDataNotSavedDialog()
                    }
                }
            } catch (exc: Throwable) {
                Log.e("ListingDetailsViewModel", exc.message.toString())
                fragment.requireActivity().runOnUiThread {
                    fragment.loadingDialog.dismiss()
                    fragment.showDataNotSavedDialog()
                }
            }
        }
    }

    fun acceptProposal(proposalId: Int, fragment: ListingDetailsFragment) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isAcceptSuccessful = repository.acceptListingProposal(proposalId)
                fragment.requireActivity().runOnUiThread {
                    fragment.loadingDialog.dismiss()
                    if (isAcceptSuccessful) {
                        fragment.onAcceptSuccessful()
                    } else {
                        fragment.showDataNotSavedDialog()
                    }
                }
            } catch (exc: Throwable) {
                Log.e("ListingDetailsViewModel", exc.message.toString())
                fragment.requireActivity().runOnUiThread {
                    fragment.loadingDialog.dismiss()
                    fragment.showDataNotSavedDialog()
                }
            }
        }
    }

    fun sendProposal(listingProposal: ListingProposal, fragment: ListingDetailsFragment) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.sendListingProposal(listingProposal)
                fragment.requireActivity().runOnUiThread {
                    fragment.loadingDialog.dismiss()
                    fragment.onProposalSaveSuccessful()
                }
            } catch (exc: Throwable) {
                Log.e("ListingDetailsViewModel", exc.message.toString())
                fragment.requireActivity().runOnUiThread {
                    fragment.loadingDialog.dismiss()
                    fragment.showProposalAlreadyExistsDialog()
                }
            }
        }
    }

    fun deleteProposal(listingId: Int, fragment: ListingDetailsFragment) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteListingProposal(listingId)
                fragment.requireActivity().runOnUiThread {
                    fragment.loadingDialog.dismiss()
                    fragment.onProposalDeleteSuccessful()
                }
            } catch (exc: Throwable) {
                Log.e("ListingDetailsViewModel", exc.message.toString())
                fragment.requireActivity().runOnUiThread {
                    fragment.loadingDialog.dismiss()
                    fragment.onProposalDeleteFailed()
                }
            }
        }
    }
}