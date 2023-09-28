package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import java.util.Locale

class individualSpeciesView : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var getSpeciesData: SpeciesClass
    private var paused: Boolean = true
    private var ttsButton: ImageButton? = null
    private var textToSpeech: TextToSpeech? = null
    private lateinit var detailName: TextView
    private lateinit var detailImage: ImageView
    private lateinit var detailDescription: TextView
    private var textBody: String = ""
    private lateinit var returnActivity: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual_species_view)

        addStarButtonListener()

        // set Toolbar text appropriately
        var toolBar: Toolbar = findViewById(R.id.materialToolbar)
        toolBar.title = getString(R.string.info)

        toolBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settingsItem -> {
                    val intent = Intent(this@individualSpeciesView, SettingsActivity::class.java)
                    startActivity(intent)
                    // top to bottom
                    this.overridePendingTransition(R.anim.slide_from_top,R.anim.slide_to_bottom)
                    return@setOnMenuItemClickListener true
                }

                R.id.helpItem -> {
                    val intent = Intent(this@individualSpeciesView, HelpActivity::class.java)
                    startActivity(intent)
                    // top to bottom
                    this.overridePendingTransition(R.anim.slide_from_top,R.anim.slide_to_bottom)
                    return@setOnMenuItemClickListener true
                }
            }
            false
        }

        returnActivity = intent.getStringExtra("returnActivity").toString()

        getSpeciesData = intent.getParcelableExtra<SpeciesClass>("species")!!
        if (getSpeciesData != null){
            detailName = findViewById(R.id.detailName)
            detailImage = findViewById(R.id.detailImage)
            detailDescription = findViewById(R.id.detailDescription)

            detailName.text = getSpeciesData.getName()
            detailDescription.text = getSpeciesData.getDesc()
            detailImage.setImageResource(getSpeciesData.getImage())
        }

        // defining text to be used for text to speech
        textBody = getSpeciesData.getDesc()

        // return to home screen
        val homeButton: ImageButton = findViewById(R.id.homeButton)
        homeButton.setOnClickListener{
            val intentHome = Intent(this@individualSpeciesView, MainActivity::class.java)
            startActivity(intentHome)
            // transition right to left
            this.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
        }


        // return to previous screen if back button pressed
        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener{
            when (returnActivity) {
                getString(R.string.scan) -> {
                    // return to scan screen
                    val intentScan = Intent(this@individualSpeciesView, Scan::class.java)
                    startActivity(intentScan)
                    // transition right to left
                    this.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                }
                getString(R.string.search_by_text) -> {
                    // return to search screen
                    val intentSearch = Intent(this@individualSpeciesView, Search::class.java)
                    startActivity(intentSearch)
                    // transition right to left
                    this.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                }
                else -> {
                    // else return to previous screen (shouldn't ever occur)
                    finish()
                    // transition right to left
                    this.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                }
            }

        }

        // text-to-speech functionality
        textToSpeech = TextToSpeech(this, this)

        ttsButton = findViewById(R.id.tssButton)
        //ttsButton.setImageResource()
        ttsButton!!.isEnabled = false

        // listener for TTS button
        ttsButton!!.setOnClickListener{
            // setting image of button appropriately

            if (paused) {
                ttsButton!!.setImageResource(R.drawable.outline_stop_circle_24)
                paused = false
                textToSpeech!!.playSilentUtterance(750, TextToSpeech.QUEUE_FLUSH, "OTHER_UTTERANCE_ID")
                speakOut()
            } else {
                resetText()
                textToSpeech!!.stop()
            }

        }

    }

    private fun addStarButtonListener(){
        val imageBtn: ImageButton = findViewById(R.id.star_button)
        imageBtn.setOnClickListener(){
            passSightingName(getSpeciesData.getName())
        }
    }

    //pass the name of the bird or plant sighted to MapActivity to place a marker on the map at the cuurent location
    private fun passSightingName(sightingName: String){
        val intentMap = Intent(this@individualSpeciesView, MapActivity::class.java)
        intentMap.putExtra("activity", "IndividualSpeciesView")
        intentMap.putExtra("sightingName", sightingName)
        startActivity(intentMap)
    }

    // sets up text-to-speech object
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The language is not supported!")
            } else {
                ttsButton!!.isEnabled = true
            }

            // Progress Listener monitors progress of text to speech through body of text
            textToSpeech!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                // TTS started
                override fun onStart(utteranceId: String) {
                    Log.i("TTS", "utterance started")
                }

                // TTS done
                override fun onDone(utteranceId: String) {
                    if (utteranceId == "UNIQUE_UTTERANCE_ID"){
                        resetText()
                    }
                }
                // TTS error
                override fun onError(utteranceId: String) {
                    Log.i("TTS", "utterance error")
                }

                // This method highlights each word being spoken by the TTS system
                override fun onRangeStart(
                    utteranceId: String,
                    start: Int,
                    end: Int,
                    frame: Int
                ) {

                    // run highlighting of spoken text on a separate thread
                    runOnUiThread {
                        val textWithHighlights: Spannable = SpannableString(textBody)
                        textWithHighlights.setSpan(
                            ForegroundColorSpan(Color.RED),
                            start,
                            end,
                            Spanned.SPAN_INCLUSIVE_INCLUSIVE
                        )
                        detailDescription!!.text = textWithHighlights
                    }
                }
            })
            // make the TTS speak a bit slower
            textToSpeech!!.setSpeechRate(getSpeechRate())
        }
    }

    private fun getSpeechRate(): Float{
        //get the speech rate set by user in settings
        val sharedPref: SharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        //return rate or default to 5 (arbitrary number)
        var rate: Float
        when (sharedPref.getInt("speech_rate", 5)) {
            0 -> rate = 0.5F // slowest
            1 -> rate = 0.6F //slow
            3 -> rate = 1F // Fast
            4 -> rate = 1.2F //Fastest
            else -> {
                rate = 0.8F //if the sharePreferences has not been saved or normal rate is set
                //0.8 is the normal speech rate
            }
        }
        return rate
    }

    // makes TTS object actually speak
    private fun speakOut() {
        textToSpeech!!.speak(textBody, TextToSpeech.QUEUE_ADD, null, "UNIQUE_UTTERANCE_ID")
    }

    override fun onStop() {
        // shutdown TTS system when activity is destroyed
        if (textToSpeech!!.isSpeaking) {
            textToSpeech!!.stop()
        }
        textToSpeech!!.shutdown()
        super.onStop()
    }

    private fun resetText() {
        // reset button state
        paused = true
        // need UI thread to make changes without any errors being thrown
        runOnUiThread{
            // change image button icon to play again
            ttsButton!!.setImageResource(R.drawable.baseline_play_circle_outline_24)
            // when done return text to non-highlighted state
            detailDescription!!.text = textBody
        }
    }


}