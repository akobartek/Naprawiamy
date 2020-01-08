package pl.sokolowskibartlomiej.naprawiamy.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Offer(
    val title: String,
    val description: String,
    val city: String?,
    val price: Double?,
    val publishDate: String,
    val completionDate: String?,
    val photos: Array<String>?
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Offer

        if (title != other.title) return false
        if (description != other.description) return false
        if (city != other.city) return false
        if (price != other.price) return false
        if (publishDate != other.publishDate) return false
        if (completionDate != other.completionDate) return false
        if (photos != null) {
            if (other.photos == null) return false
            if (!photos.contentEquals(other.photos)) return false
        } else if (other.photos != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + (city?.hashCode() ?: 0)
        result = 31 * result + (price?.hashCode() ?: 0)
        result = 31 * result + publishDate.hashCode()
        result = 31 * result + (completionDate?.hashCode() ?: 0)
        result = 31 * result + (photos?.contentHashCode() ?: 0)
        return result
    }
}