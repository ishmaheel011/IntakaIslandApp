package com.example.myapplication

import android.os.Parcel
import android.os.Parcelable

// Class that stores the image, name, and description associated
// with each card (of a bird or plant) on the search screen and
// species on the individualSpeciesView activity
data class SpeciesClass(private var speciesImage:Int, private var speciesText:String, private var description:String): Parcelable {
    // all methods that aren't getters, setters, or equals
    // are required to be overridden as a result of
    // extending the Parcelable interface
    // all are pretty straightforward
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(speciesImage)
        parcel.writeString(speciesText)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SpeciesClass> {
        override fun createFromParcel(parcel: Parcel): SpeciesClass {
            return SpeciesClass(parcel)
        }

        override fun newArray(size: Int): Array<SpeciesClass?> {
            return arrayOfNulls(size)
        }
    }

    // default methods
    fun getName():String {
        return this.speciesText
    }

    fun getImage():Int {
        return this.speciesImage
    }

    fun getDesc():String {
        return this.description
    }

    fun setName(s: String) {
        this.speciesText = s
    }

    fun setImage(i: Int) {
        this.speciesImage = i
    }

    fun setDesc(d: String) {
        this.description = d
    }

    fun equals(otherSpecies: SpeciesClass): Boolean{

        if (this.speciesText != otherSpecies.speciesText) {return false}
        if (this.speciesImage != otherSpecies.speciesImage) {return false}
        if (this.description != otherSpecies.description) {return false}
        return true
    }

    // toString is implicitly provided so will not be overridden

}
