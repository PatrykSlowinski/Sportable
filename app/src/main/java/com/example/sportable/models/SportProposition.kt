package com.example.sportable.models

import android.os.Parcel
import android.os.Parcelable

data class SportProposition(
    val name: String = ""
): Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString()!!) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SportProposition> {
        override fun createFromParcel(parcel: Parcel): SportProposition {
            return SportProposition(parcel)
        }

        override fun newArray(size: Int): Array<SportProposition?> {
            return arrayOfNulls(size)
        }
    }
}
