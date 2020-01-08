package pl.sokolowskibartlomiej.naprawiamy.apicalls

import okhttp3.MultipartBody
import pl.sokolowskibartlomiej.naprawiamy.model.*
import retrofit2.Response
import retrofit2.http.*

interface NaprawiamyApi {

    // region Categories
    @GET("Categories")
    suspend fun getCategories(): List<Category>

    @GET("Categories/{parentId}")
    suspend fun getCategoriesByParentId(@Path("parentId") parentId: Int): List<Category>
    // endregion

    // region SpecialistCategories
    @PUT("SpecialistCategories/{categoryId}")
    suspend fun updateSpecialistCategories(@Path("categoryId") categoryId: Int): SpecialistCategory

    @DELETE("SpecialistCategories/{categoryId}")
    suspend fun deleteSpecialistCategory(@Path("categoryId") categoryId: Int): Response<Void>

    @GET("SpecialistCategories")
    suspend fun getSpecialistCategories(): List<Category>
    // endregion

    // region Images
    @PUT("Images")
    suspend fun addImageWithUrl(@Body url: String): Image

    @DELETE("Images/{imageId}")
    suspend fun deleteImage(@Path("imageId") imageId: Int): Response<Void>

    @GET("Images")
    suspend fun getUserImages(): List<Image>

    @GET("Images/{imageId}")
    suspend fun getImage(@Path("imageId") imageId: Int): Image

    @Multipart
    @POST("Images/Upload")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Image

    @GET("Images/Download/{filename}")
    suspend fun downloadImage(@Path("filename") filename: String): Response<Void>
    // endregionImages

    // region ListingImages
    @PUT("ListingImages")
    suspend fun addListingImage(@Body listingImage: ListingImage): ListingImage

    @DELETE("ListingImages/{listingImageId}")
    suspend fun deleteListingImages(@Path("listingImageId") listingImageId: Int): Response<Void>

    @GET("ListingImages/{listingId}")
    suspend fun getListingImages(@Path("listingId") listingId: Int): List<ListingImage>
    // endregion ListingImages

    // region ListingProposals
    @PUT("ListingProposals")
    suspend fun sendListingProposal(@Body proposal: ListingProposal): ListingProposal

    @DELETE("ListingProposals/{listingId}")
    suspend fun deleteListingProposal(@Path("listingId") listingId: Int): Response<Void>

    @GET("ListingProposals")
    suspend fun getListingProposals(): List<ListingProposal>

    @GET("ListingProposals/{listingId}")
    suspend fun getListingProposalForListing(@Path("listingId") listingId: Int): List<ListingProposal>

    @GET("ListingProposals/User/{listingProposalId}")
    suspend fun getSpecialistInfoByProposal(@Path("listingProposalId") listingProposalId: Int): User
    // endregion

    // region Listings
    @PUT("Listings")
    suspend fun addListing(@Body listing: Listing): Listing

    @PATCH("Listings")
    suspend fun updateListing(@Body listing: Listing): Response<Void>

    @GET("Listings/Mine")
    suspend fun getMyListings(): List<Listing>

    @GET("Listings/Open")
    suspend fun getOpenListings(): List<Listing>

    @PATCH("Listings/Accept/{listingProposalId}")
    suspend fun acceptListingProposal(@Path("listingProposalId") listingProposalId: Int): Response<Void>
    // endregion

    // region ListingVotes
    @PUT("ListingVotes/{listingId}/{rating}")
    suspend fun addListingVote(@Path("listingId") listingId: Int, @Path("rating") rating: Int): ListingVote

    @DELETE("ListingVotes/{listingId}")
    suspend fun deleteListingVote(@Path("listingId") listingId: Int): Response<Void>

    @GET("ListingVotes")
    suspend fun getListingVotes(): List<ListingVote>

    @GET("ListingVotes/{userId}")
    suspend fun getUserListingVotes(@Path("userId") userId: Int): List<ListingVote>
    // endregion

    // region Users
    @PUT("Users")
    suspend fun signUpUserAsync(@Body authInfo: AuthInfo): Response<Void>

    @POST("Users/Authenticate")
    suspend fun authenticateUserAsync(@Body authInfo: AuthInfo): User

    @PATCH("Users")
    suspend fun updateUserInfoAsync(@Body user: User): Response<Void>
    // endregion
}