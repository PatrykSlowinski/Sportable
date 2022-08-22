package com.example.sportable.models

import android.os.Parcel
import android.os.Parcelable

data class LastFilters(
    val userId: String = "",
    val sports: ArrayList<String> = ArrayList()
) : Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.createStringArrayList()!!

    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeStringList(sports)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LastFilters> {
        override fun createFromParcel(parcel: Parcel): LastFilters {
            return LastFilters(parcel)
        }

        override fun newArray(size: Int): Array<LastFilters?> {
            return arrayOfNulls(size)
        }
    }

}
