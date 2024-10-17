package com.photopicker

import android.net.Uri
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.facebook.react.bridge.*

class PhotoPickerModule(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private var promise: Promise? = null

  override fun getName(): String {
    return "PhotoPicker"
  }

  @ReactMethod
  fun launchPicker(options: ReadableMap?, promise: Promise) {
    this.promise = promise
    val currentActivity = currentActivity

    if (currentActivity == null || currentActivity !is FragmentActivity) {
      promise.reject("E_ACTIVITY_DOES_NOT_EXIST", "Activity doesn't exist or is not a FragmentActivity")
      return
    }

    val fragmentManager: FragmentManager = currentActivity.supportFragmentManager

    val existingFragment = fragmentManager.findFragmentByTag("PhotoPickerFragment")
    if (existingFragment != null) {
      // Fragment is already added
      return
    }

    // Extract quality and minSize from options if provided
    val maxSize = if (options != null && options.hasKey("maxSize")) options.getInt("maxSize") else null

    Log.d("[PhotoPicker]", "Creating Fragment with maxSize=$maxSize")
    val fragment = PhotoPickerFragment().apply {
      this.maxSize = maxSize
    }

    fragment.callback = object : PhotoPickerFragment.PhotoPickerCallback {
      override fun onPhotoPicked(result: PhotoPickerResult) {
        Log.d("[PhotoPicker][PhotoPickerFragment]", "onPhotoPicked ${result.uri} ${result.width}x${result.height}")
        val resultMap = Arguments.createMap().apply {
          putString("uri", result.uri.toString())
          putInt("width", result.width)
          putInt("height", result.height)
          putDouble("fileSize", result.fileSize.toDouble())
          if (result.exif != null) {
            val exifMap = Arguments.createMap()
            for ((key, value) in result.exif) {
              exifMap.putString(key, value)
            }
            putMap("exif", exifMap)
          }
        }
        promise.resolve(resultMap)
        this@PhotoPickerModule.promise = null
      }

      override fun onPhotoPickerCancelled() {
        Log.d("[PhotoPicker][PhotoPickerFragment]", "onPhotoPickerCancelled")
        promise.resolve(null)
        this@PhotoPickerModule.promise = null
      }
    }

    fragmentManager.beginTransaction()
      .add(fragment, "PhotoPickerFragment")
      .commitAllowingStateLoss()
  }
}





