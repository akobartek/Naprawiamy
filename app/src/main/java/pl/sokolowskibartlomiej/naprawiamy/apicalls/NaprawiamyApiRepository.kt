package pl.sokolowskibartlomiej.naprawiamy.apicalls

import okhttp3.MultipartBody
import pl.sokolowskibartlomiej.naprawiamy.apicalls.RetrofitClient.authorizedNaprawiamyApi
import pl.sokolowskibartlomiej.naprawiamy.apicalls.RetrofitClient.naprawiamyApi
import pl.sokolowskibartlomiej.naprawiamy.model.*

class NaprawiamyApiRepository {

    // region Categories
    suspend fun getCategories(): Int = authorizedNaprawiamyApi.getCategories()

    suspend fun getCategories(offset: Int, count: Int): List<Category> =
        authorizedNaprawiamyApi.getCategories(offset, count)

    suspend fun getCategoriesByParentId(parentId: Int): List<Category> =
        authorizedNaprawiamyApi.getCategoriesByParentId(parentId)
    // endregion

    // region SpecialistCategories
    suspend fun updateSpecialistCategories(categoryId: Int) =
        authorizedNaprawiamyApi.updateSpecialistCategories(categoryId)

    suspend fun deleteSpecialistCategory(categoryId: Int): Boolean =
        authorizedNaprawiamyApi.deleteSpecialistCategory(categoryId).isSuccessful

    suspend fun getSpecialistCategories() = authorizedNaprawiamyApi.getSpecialistCategories()
    suspend fun getSpecialistCategories(offset: Int, count: Int) =
        authorizedNaprawiamyApi.getSpecialistCategories(offset, count)
    // endregion

    // region Images
    suspend fun addImageWithUrl(url: String) = authorizedNaprawiamyApi.addImageWithUrl(url)

    suspend fun deleteImage(imageId: Int): Boolean =
        authorizedNaprawiamyApi.deleteImage(imageId).isSuccessful

    suspend fun getUserImages() = authorizedNaprawiamyApi.getUserImages()
    suspend fun getUserImages(offset: Int, count: Int) =
        authorizedNaprawiamyApi.getUserImages(offset, count)

    suspend fun getImage(imageId: Int) = authorizedNaprawiamyApi.getImage(imageId)

    suspend fun uploadImage(file: MultipartBody.Part) = authorizedNaprawiamyApi.uploadImage(file)

    suspend fun downloadImage(filename: String): Boolean =
        authorizedNaprawiamyApi.downloadImage(filename).isSuccessful
    // endregionImages

    // region ListingImages
    suspend fun addListingImage(listingImage: ListingImage) =
        authorizedNaprawiamyApi.addListingImage(listingImage)

    suspend fun deleteListingImages(listingImageId: Int): Boolean =
        authorizedNaprawiamyApi.deleteListingImages(listingImageId).isSuccessful

    suspend fun getListingImages(listingId: Int) =
        authorizedNaprawiamyApi.getListingImages(listingId)
    // endregion ListingImages

    // region ListingProposals
    suspend fun sendListingProposal(proposal: ListingProposal) =
        authorizedNaprawiamyApi.sendListingProposal(proposal)

    suspend fun deleteListingProposal(listingId: Int): Boolean =
        authorizedNaprawiamyApi.deleteListingProposal(listingId).isSuccessful

    suspend fun getListingProposals() = authorizedNaprawiamyApi.getListingProposals()
    suspend fun getListingProposals(offset: Int, count: Int) =
        authorizedNaprawiamyApi.getListingProposals(offset, count)

    suspend fun getListingProposalForListing(listingId: Int) =
        authorizedNaprawiamyApi.getListingProposalForListing(listingId)

    suspend fun getSpecialistInfoByProposal(listingProposalId: Int) =
        authorizedNaprawiamyApi.getSpecialistInfoByProposal(listingProposalId)
    // endregion

    // region Listings
    suspend fun saveListing(listing: Listing) = authorizedNaprawiamyApi.addListing(listing)

    suspend fun updateListing(listing: Listing) =
        authorizedNaprawiamyApi.updateListing(listing).isSuccessful

    suspend fun getMyListings() = authorizedNaprawiamyApi.getMyListings()

    suspend fun getOpenListings() = authorizedNaprawiamyApi.getOpenListings()
    suspend fun getOpenListings(offset: Int, count: Int) =
        authorizedNaprawiamyApi.getOpenListings(offset, count)

    suspend fun acceptListingProposal(listingProposalId: Int): Boolean =
        authorizedNaprawiamyApi.acceptListingProposal(listingProposalId).isSuccessful
    // endregion

    // region ListingVotes
    suspend fun addListingVote(listingId: Int, rating: Int) =
        authorizedNaprawiamyApi.addListingVote(listingId, rating)

    suspend fun deleteListingVote(listingId: Int): Boolean =
        authorizedNaprawiamyApi.deleteListingVote(listingId).isSuccessful

    suspend fun getListingVotes() = authorizedNaprawiamyApi.getListingVotes()
    suspend fun getListingVotes(offset: Int, count: Int) =
        authorizedNaprawiamyApi.getListingVotes(offset, count)

    suspend fun getUserListingVotes(userId: Int) =
        authorizedNaprawiamyApi.getUserListingVotes(userId)

    suspend fun getUserListingVotes(userId: Int, offset: Int, count: Int) =
        authorizedNaprawiamyApi.getUserListingVotes(userId, offset, count)
    // endregion

    // region Users
    suspend fun signUpUser(special: Boolean, email: String, password: String): User? {
        return if (naprawiamyApi.signUpUserAsync(AuthInfo(special, email, password)).isSuccessful)
            authenticateUser(special, email, password)
        else null
    }

    suspend fun authenticateUser(special: Boolean, email: String, password: String) =
        naprawiamyApi.authenticateUserAsync(AuthInfo(special, email, password))

    suspend fun updateUserInfo(user: User): Boolean? =
        authorizedNaprawiamyApi.updateUserInfoAsync(user).isSuccessful
    // endregion
}