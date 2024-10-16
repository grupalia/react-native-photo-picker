package com.photopicker

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.fragment.app.Fragment

class PhotoPickerFragment : Fragment() {

  interface PhotoPickerCallback {
    fun onPhotoPicked(uri: Uri, width: Int, height: Int)
    fun onPhotoPickerCancelled()
  }

  var callback: PhotoPickerCallback? = null

  private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
    if (uri != null) {
      Log.d("[PhotoPicker][PhotoPickerFragment]", "Selected URI: $uri")
      val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
      }
      requireContext().contentResolver.openInputStream(uri).use { inputStream ->
        BitmapFactory.decodeStream(inputStream, null, options)
      }
      val width = options.outWidth
      val height = options.outHeight

      // Pass the URI and dimensions back through the callback
      callback?.onPhotoPicked(uri, width, height)
    } else {
      Log.d("[PhotoPicker][PhotoPickerFragment]", "No media selected")
      callback?.onPhotoPickerCancelled()
    }
    // Remove the fragment after completion
    parentFragmentManager.beginTransaction().remove(this).commitAllowingStateLoss()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Launch the photo picker
    pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
  }
}
