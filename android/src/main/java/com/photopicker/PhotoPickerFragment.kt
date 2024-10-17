package com.photopicker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileOutputStream

data class PhotoPickerResult(
  val uri: Uri,
  val width: Int,
  val height: Int,
  val fileSize: Long,
  val exif: Map<String, String>?
)

data class AdjustedImageResult(
  val uri: Uri,
  val exif: Map<String, String>?,
  val fileSize: Long
)

class PhotoPickerFragment : Fragment() {

  interface PhotoPickerCallback {
    fun onPhotoPicked(result: PhotoPickerResult)
    fun onPhotoPickerCancelled()
  }

  var callback: PhotoPickerCallback? = null
  var maxSize: Int? = null  // Max size in pixels (width or height, depending on orientation)

  private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
    if (uri != null) {
      Log.d("[PhotoPicker][PhotoPickerFragment]", "Selected URI: $uri")

      // Adjust the image quality
      val adjustedImageResult = adjustImageQuality(requireContext(), uri, maxSize)
      if (adjustedImageResult != null) {
        // Get the dimensions of the adjusted image
        val options = BitmapFactory.Options().apply {
          inJustDecodeBounds = true
        }
        requireContext().contentResolver.openInputStream(adjustedImageResult.uri).use { inputStream ->
          BitmapFactory.decodeStream(inputStream, null, options)
        }
        val width = options.outWidth
        val height = options.outHeight

        // Create the result object
        val result = PhotoPickerResult(
          uri = adjustedImageResult.uri,
          width = width,
          height = height,
          fileSize = adjustedImageResult.fileSize,
          exif = adjustedImageResult.exif
        )

        // Pass the result back through the callback
        callback?.onPhotoPicked(result)
      } else {
        Log.d("[PhotoPicker][PhotoPickerFragment]", "Failed to adjust image quality")
        callback?.onPhotoPickerCancelled()
      }
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

  private fun adjustImageQuality(
    context: Context,
    uri: Uri,
    maxSize: Int?
  ): AdjustedImageResult? {
    try {
      // Read EXIF data from the original image
      val exifData = readExifData(context, uri)

      // Get the size of the original image
      val originalFileSize = context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0

      if (maxSize != null) {
        // Apply the specified quality directly
        return compressImage(context, uri, maxSize, exifData)
      } else {
        // Neither quality nor minSize is provided, return original image
        return AdjustedImageResult(uri, exifData, originalFileSize)
      }
    } catch (e: Exception) {
      e.printStackTrace()
      return null
    }
  }

  private fun rotateBitmapIfRequired(bitmap: Bitmap, orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
      ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
      ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
      ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
      ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
      ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
      ExifInterface.ORIENTATION_TRANSPOSE -> {
        matrix.postRotate(90f)
        matrix.preScale(-1f, 1f)
      }
      ExifInterface.ORIENTATION_TRANSVERSE -> {
        matrix.postRotate(270f)
        matrix.preScale(-1f, 1f)
      }
      // ExifInterface.ORIENTATION_NORMAL or undefined
      else -> return bitmap
    }

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
  }

  private fun readExifData(context: Context, uri: Uri): Map<String, String>? {
    return try {
      val exifInterface = context.contentResolver.openInputStream(uri)?.use { inputStream ->
        ExifInterface(inputStream)
      } ?: return null

      val exifTags = listOf(
        ExifInterface.TAG_APERTURE_VALUE,
        ExifInterface.TAG_DATETIME,
        ExifInterface.TAG_EXPOSURE_TIME,
        ExifInterface.TAG_FLASH,
        ExifInterface.TAG_FOCAL_LENGTH,
        ExifInterface.TAG_GPS_ALTITUDE,
        ExifInterface.TAG_GPS_ALTITUDE_REF,
        ExifInterface.TAG_GPS_DATESTAMP,
        ExifInterface.TAG_GPS_LATITUDE,
        ExifInterface.TAG_GPS_LATITUDE_REF,
        ExifInterface.TAG_GPS_LONGITUDE,
        ExifInterface.TAG_GPS_LONGITUDE_REF,
        ExifInterface.TAG_GPS_PROCESSING_METHOD,
        ExifInterface.TAG_GPS_TIMESTAMP,
        ExifInterface.TAG_MAKE,
        ExifInterface.TAG_MODEL,
        ExifInterface.TAG_WHITE_BALANCE
        // Add other tags as needed
      )

      val exifData = mutableMapOf<String, String>()
      for (tag in exifTags) {
        val value = exifInterface.getAttribute(tag)
        if (value != null) {
          exifData[tag] = value
        }
      }
      exifData
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  private fun compressImage(
    context: Context,
    uri: Uri,
    maxSize: Int,
    exifData: Map<String, String>?
  ): AdjustedImageResult? {
    try {
      // Open InputStream to read EXIF data
      val inputStreamForExif = context.contentResolver.openInputStream(uri)
      val exif = inputStreamForExif?.let {
        ExifInterface(it)
      }
      inputStreamForExif?.close()

      // Get the orientation from EXIF data
      val orientation = exif?.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
      ) ?: ExifInterface.ORIENTATION_NORMAL

      // Decode the image from the URI
      var bitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BitmapFactory.decodeStream(inputStream)
      } ?: return null

      // Rotate the bitmap if necessary
      val rotatedBitmap = rotateBitmapIfRequired(bitmap, orientation)
      // Create a temporary file to save the adjusted image
      val tempFile = File.createTempFile("adjusted_image", ".jpg", context.cacheDir)

      // Compress and write the scaled bitmap to the tempFile
      FileOutputStream(tempFile).use { outStream ->
        if (rotatedBitmap.width > maxSize || rotatedBitmap.height > maxSize) {
          bitmap = getResizedBitmap(rotatedBitmap, maxSize)
        }

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
      }

      // Get the size of the compressed image
      val compressedFileSize = tempFile.length()

      // Recycle bitmaps to free memory
      if (rotatedBitmap != bitmap) {
        bitmap.recycle()
      }
      rotatedBitmap.recycle()

      // Copy EXIF data to the new image
      copyExifData(context, uri, tempFile)

      // Return the adjusted image result
      return AdjustedImageResult(Uri.fromFile(tempFile), exifData, compressedFileSize)
    } catch (e: Exception) {
      e.printStackTrace()
      return null
    }
  }

  private fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
    var width = image.width
    var height = image.height
    val bitmapRatio = width.toFloat() / height.toFloat()
    if (bitmapRatio > 1) {
      width = maxSize
      height = (width / bitmapRatio).toInt()
    } else {
      height = maxSize
      width = (height * bitmapRatio).toInt()
    }
    return Bitmap.createScaledBitmap(image, width, height, true)
  }

  private fun copyExifData(context: Context, sourceUri: Uri, destFile: File) {
    try {
      val originalExif = context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
        ExifInterface(inputStream)
      } ?: return

      val newExif = ExifInterface(destFile.absolutePath)

      val attributes = listOf(
        ExifInterface.TAG_APERTURE_VALUE,
        ExifInterface.TAG_DATETIME,
        ExifInterface.TAG_EXPOSURE_TIME,
        ExifInterface.TAG_FLASH,
        ExifInterface.TAG_FOCAL_LENGTH,
        ExifInterface.TAG_GPS_ALTITUDE,
        ExifInterface.TAG_GPS_ALTITUDE_REF,
        ExifInterface.TAG_GPS_DATESTAMP,
        ExifInterface.TAG_GPS_LATITUDE,
        ExifInterface.TAG_GPS_LATITUDE_REF,
        ExifInterface.TAG_GPS_LONGITUDE,
        ExifInterface.TAG_GPS_LONGITUDE_REF,
        ExifInterface.TAG_GPS_PROCESSING_METHOD,
        ExifInterface.TAG_GPS_TIMESTAMP,
        ExifInterface.TAG_MAKE,
        ExifInterface.TAG_MODEL,
        ExifInterface.TAG_WHITE_BALANCE
      )

      for (attribute in attributes) {
        val value = originalExif.getAttribute(attribute)
        if (value != null) {
          newExif.setAttribute(attribute, value)
        }
      }
      // Do not copy the orientation tag since we have already adjusted the image
      newExif.saveAttributes()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}



