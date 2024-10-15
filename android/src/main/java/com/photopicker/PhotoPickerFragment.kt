package com.photopicker

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.fragment.app.Fragment

class PhotoPickerFragment : Fragment() {

  interface PhotoPickerCallback {
    fun onPhotoPicked(uri: Uri)
    fun onPhotoPickerCancelled()
  }

  var callback: PhotoPickerCallback? = null

  private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
    if (uri != null) {
      Log.d("[PhotoPicker][PhotoPickerFragment]", "Selected URI: $uri")
      callback?.onPhotoPicked(uri)
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
