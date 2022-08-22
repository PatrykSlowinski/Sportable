package com.example.sportable.models

import android.os.Parcel
import android.os.Parcelable

data class Sport(
    val name: String = "",
    val image: String = "",
    var documentId: String = ""
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )


    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        parcel.writeString(name)
        parcel.writeString(image)
        parcel.writeString(documentId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Sport> {
        override fun createFromParcel(parcel: Parcel): Sport {
            return Sport(parcel)
        }

        override fun newArray(size: Int): Array<Sport?> {
            return arrayOfNulls(size)
        }
    }
}
