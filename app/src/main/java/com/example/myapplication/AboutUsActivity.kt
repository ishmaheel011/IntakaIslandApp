package com.example.myapplication

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class AboutUsActivity:AppCompatActivity() {
    /**
     * sets listeners for UI components
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_us)

        // return to previous screen if back button pressed
        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener{
            finish()
            // bottom to top
            this.overridePendingTransition(R.anim.slide_from_bottom,R.anim.slide_to_top)
        }

        // set Toolbar text appropriately
        val toolBar: Toolbar = findViewById(R.id.materialToolbar)
        toolBar.title = getString(R.string.aboutUs)
    }
}