package com.photopicker

import android.net.Uri
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class PhotoPickerModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

  private var promise: Promise? = null

  override fun getName(): String {
    return "PhotoPicker"
  }

  @ReactMethod
  fun launchPicker(promise: Promise) {
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

    Log.d("[PhotoPicker]", "Creating Fragment")
    val fragment = PhotoPickerFragment()
    fragment.callback = object : PhotoPickerFragment.PhotoPickerCallback {
      override fun onPhotoPicked(uri: Uri) {
        Log.d("[PhotoPicker][PhotoPickerFragment]", "onPhotoPicked $uri")
        promise.resolve(uri.toString())
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


