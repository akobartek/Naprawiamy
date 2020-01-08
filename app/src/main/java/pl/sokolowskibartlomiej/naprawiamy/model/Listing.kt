package pl.sokolowskibartlomiej.naprawiamy.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Listing(
    val id: Int?,
    val clientId: Int?,
    val categoryId: Int?,
    val title: String?,
    val description: String?,
    val address: String?,
    val proposedValue: Double?,
    val maxDeadline: Date?,
    val acceptedProposalId: Int?
) : Parcelable

data class ListingProposal(
    val id: Int?,
    val listingId: Int,
    val specialistId: Int,
    val offeredValue: Double,
    val offeredDeadline: Date
)

data class ListingVote(
    val id: Int,
    val rating: Int,
    val listingProposalId: Int
)

data class ListingImage(
    val id: Int,
    val listingId: Int,
    val imageId: Int
)

data class Image(
    val id: Int,
    val userId: Int,
    val url: String
)