package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HelpActivity : AppCompatActivity() {

    private val hAdapter: homeAdapter by lazy { homeAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.help)

        // return to previous screen if back button pressed
        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener{
            finish()
            // bottom to top anim
            this.overridePendingTransition(R.anim.slide_from_bottom,R.anim.slide_to_top)
        }

        // set Toolbar text appropriately
        var toolBar: Toolbar = findViewById(R.id.materialToolbar)
        toolBar.title = getString(R.string.help)


        //populating cards
        runOnUiThread {
            hAdapter.setData(getCards())

            // updating layout accordingly
            findViewById<RecyclerView>(R.id.recyclerView).apply {
                layoutManager = LinearLayoutManager(this@HelpActivity)
                hasFixedSize()
                this.adapter = hAdapter
            }
        }



    }

    private fun getCards(): List<homeCards> = listOf(
        // all the cards displayed on the help screen

        // Home help
        homeCards.helpCard(
            image = R.drawable.baseline_home_24,
            help = getString(R.string.homeHelp),
            title = getString(R.string.home)
        ),
        // Scan help
        homeCards.helpCard(
            image = R.drawable.baseline_camera_alt_24,
            help = getString(R.string.scanHelp),
            title = getString(R.string.scan)
        ),
        // Map help
        homeCards.helpCard(
            image = R.drawable.baseline_location_on_24,
            help = getString(R.string.mapHelp),
            title = getString(R.string.maps)
        ),
        // Search help
        homeCards.helpCard(
            image = R.drawable.baseline_search_24,
            help = getString(R.string.searchHelp),
            title = getString(R.string.search_by_text)
        ),
        // Text to speech help
        homeCards.helpCard(
            image = R.drawable.baseline_mic_24,
            help = getString(R.string.ttsHelp),
            title = getString(R.string.ttsHeader)
        ),
        // Settings help
        homeCards.helpCard(
            image = R.drawable.baseline_settings_24,
            help = getString(R.string.settingsHelp),
            title = getString(R.string.settings)
        ),

        )

}