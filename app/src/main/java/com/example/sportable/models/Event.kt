package com.example.sportable.models

import android.os.Parcel
import android.os.Parcelable

data class Event (
    val sportId: String = "",
    val createdBy: String = "",
    var assignedTo: ArrayList<String> = ArrayList(),
    val location: String = "",
    // godziny
    val minPeople: Int = 0,
    val maxPeople: Int = 0,
    var currentNumberOfPeople: Int = 0,
    var documentId: String = "",
    val date: Long = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val duration: Int = 0
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readInt()

    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel){
        parcel.writeString(sportId)
        parcel.writeString(createdBy)
        parcel.writeStringList(assignedTo)
        parcel.writeString(location)
        parcel.writeInt(minPeople)
        parcel.writeInt(maxPeople)
        parcel.writeInt(currentNumberOfPeople)
        parcel.writeString(documentId)
        parcel.writeLong(date)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeInt(duration)
    }

    override fun describeContents(): Int {
        return 0
    }


    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Event> = object : Parcelable.Creator<Event> {
            override fun createFromParcel(source: Parcel): Event = Event(source)
            override fun newArray(size: Int): Array<Event?> = arrayOfNulls(size)
        }
    }
}
