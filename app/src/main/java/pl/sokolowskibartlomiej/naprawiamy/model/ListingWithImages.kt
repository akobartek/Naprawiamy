package pl.sokolowskibartlomiej.naprawiamy.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ListingWithImages(
    val listing: Listing,
    var images: String = ""
): Parcelable