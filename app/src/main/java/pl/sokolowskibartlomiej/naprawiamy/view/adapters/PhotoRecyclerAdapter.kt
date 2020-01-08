package pl.sokolowskibartlomiej.naprawiamy.view.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_photo.view.*
import pl.sokolowskibartlomiej.naprawiamy.R

class PhotoRecyclerAdapter(val hideEmptyList: () -> Unit) :
    RecyclerView.Adapter<PhotoRecyclerAdapter.PhotoViewHolder>() {

    private var mPhotos = arrayListOf<Uri>()
    var isDeletingAllowed = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder =
        PhotoViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_photo,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) =
        holder.bindView(mPhotos[position])

    override fun getItemCount(): Int = mPhotos.size

    fun getPhotosList() = mPhotos
    fun setPhotosList(list: List<Uri>) {
        mPhotos = ArrayList(list)
        notifyDataSetChanged()
    }


    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(photoUri: Uri) {
            itemView.listingPhoto.setImageURI(photoUri)
            if (!isDeletingAllowed) {
                itemView.deletePhotoBtn.visibility = View.GONE
            } else {
                itemView.deletePhotoBtn.visibility = View.VISIBLE
                itemView.deletePhotoBtn.setOnClickListener {
                    mPhotos.remove(photoUri)
                    notifyDataSetChanged()
                    if (mPhotos.size == 0) hideEmptyList()
                }
            }
        }
    }
}