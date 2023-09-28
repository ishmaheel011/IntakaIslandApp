package com.example.myapplication

import android.graphics.Bitmap

// singleton object to hold image captured by a user on the scan screen
// can then be accessed across other activities
object CapturedImage {
    var bitmap: Bitmap? = null
}