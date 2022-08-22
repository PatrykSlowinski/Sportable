package com.example.sportable.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.example.sportable.activities.MyProfileActivity

object Constants {


    const val ADDRESS:String = "address"
    const val LASTFILTERS: String = "last_filters"
    const val USERS: String = "users"

    const val SPORTS: String = "sports"
    const val EVENTS: String = "events"
    const val LOCATION: String = "location"

    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobile"
    const val LOGIN: String = "login"
    const val  READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2
    const val ASSIGNED_TO: String = "assignedTo"
    const val DOCUMENT_ID: String = "documentId"
    const val SPORTS_LIST: String = "sports_list"
    const val SELECT: String = "Select"
    const val UN_SELECT: String = "UnSelect"
    const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3

    fun showImageChooser(activity: Activity) {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }
    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }


}