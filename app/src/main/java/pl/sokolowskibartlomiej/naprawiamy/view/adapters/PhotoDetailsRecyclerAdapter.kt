package pl.sokolowskibartlomiej.naprawiamy.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_photo.view.*
import pl.sokolowskibartlomiej.naprawiamy.R
import pl.sokolowskibartlomiej.naprawiamy.apicalls.RetrofitClient.BASE_API_URL
import pl.sokolowskibartlomiej.naprawiamy.utils.GlideApp

class PhotoDetailsRecyclerAdapter(val isDeletingAllowed: Boolean, val hideEmptyList: () -> Unit) :
    RecyclerView.Adapter<PhotoDetailsRecyclerAdapter.PhotoViewHolder>() {

    private var mPhotos = arrayListOf<String>()

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
    fun setPhotosList(list: List<String>) {
        mPhotos = ArrayList(list)
        notifyDataSetChanged()
    }


    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(photoUrl: String) {
            GlideApp.with(itemView.context)
                .load(
                    BASE_API_URL.substring(0, BASE_API_URL.length - 1) + photoUrl.split("~")[1]
                )
                .placeholder(R.drawable.ic_no_photo)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(itemView.listingPhoto)
            if (!isDeletingAllowed) {
                itemView.deletePhotoBtn.visibility = View.GONE
            } else {
                itemView.deletePhotoBtn.visibility = View.VISIBLE
                itemView.deletePhotoBtn.setOnClickListener {
                    mPhotos.remove(photoUrl)
                    notifyDataSetChanged()
                    if (mPhotos.size == 0) hideEmptyList()
                }
            }
        }
    }
}