package pl.sokolowskibartlomiej.naprawiamy.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import pl.sokolowskibartlomiej.naprawiamy.apicalls.NaprawiamyApiRepository
import pl.sokolowskibartlomiej.naprawiamy.model.Listing
import pl.sokolowskibartlomiej.naprawiamy.model.ListingImage
import pl.sokolowskibartlomiej.naprawiamy.utils.FileUtils
import pl.sokolowskibartlomiej.naprawiamy.view.fragments.ListingAddFragment
import java.io.File
import java.io.FileOutputStream

class ListingAddViewModel(val app: Application) : AndroidViewModel(app) {

    private val repository = NaprawiamyApiRepository()

    fun saveListing(listing: Listing, photos: ArrayList<Uri>, fragment: ListingAddFragment) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val savedListing = repository.saveListing(listing)
                photos.forEach {
                    val file = FileUtils.getFile(fragment.requireContext(), it)
//                    var isFileConverted = false
//                    if (file.extension.toLowerCase() != "png") {
//                        val originalBitmap = BitmapFactory.decodeFile(file.path)
//                        val convertedFile = File(
//                            file.absolutePath.replace(
//                                file.name, "/converted_${file.nameWithoutExtension}.png"
//                            )
//                        )
//                        val outStream = FileOutputStream(convertedFile)
//                        isFileConverted =
//                            originalBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
//                        file = convertedFile
//                        outStream.flush()
//                    }
                    val requestImageFile =
                        RequestBody.create(MediaType.parse(FileUtils.MIME_TYPE_IMAGE), file)
                    val multipartBody =
                        MultipartBody.Part.createFormData("file", file.name, requestImageFile)
                    val image = repository.uploadImage(multipartBody)
                    repository.addListingImage(ListingImage(0, savedListing.id!!, image.id))
//                    if (isFileConverted) file.delete()
                }
                fragment.requireActivity().runOnUiThread {
                    fragment.loadingDialog.dismiss()
                    fragment.onSaveSuccessful()
                }
            } catch (exc: Throwable) {
                Log.e("ListingAddViewModel", exc.toString())
                fragment.requireActivity().runOnUiThread {
                    fragment.loadingDialog.dismiss()
                    fragment.showDataNotSavedDialog()
                }
            }
        }
    }
}