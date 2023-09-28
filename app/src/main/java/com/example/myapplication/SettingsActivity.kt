package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import java.io.File

class SettingsActivity : AppCompatActivity(){
    private lateinit var speechRate: SeekBar
    private lateinit var sharedPref: SharedPreferences
    private lateinit var autoSaveSwitch: Switch
    private lateinit var clearData: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        // return to previous screen if back button pressed
        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener{
            finish()
            // bottom to top
            this.overridePendingTransition(R.anim.slide_from_bottom,R.anim.slide_to_top)
        }

        // set Toolbar text appropriately
        val toolBar: Toolbar = findViewById(R.id.materialToolbar)
        toolBar.title = getString(R.string.settings)

        speechRate = findViewById(R.id.speechRateSeekBar)
        sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        val editor: SharedPreferences.Editor = sharedPref.edit()

        autoSaveSwitch = findViewById(R.id.autoSaveSwitch)
        //save the state of the auto save switch
        autoSaveSwitch.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("auto_save", isChecked)
            editor.apply()
        }

        //Set up the speechRate seekBar
        speechRate.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var progressChangedValue = 0
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                progressChangedValue = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //No need to implement this function
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //Save the speech rate setting
                editor.putInt("speech_rate", progressChangedValue)
                editor.apply()
            }
        })

        //set the default or sharedPreferences values for the seekbar and auto-save switch
        configureSettings()

        clearData = findViewById(R.id.btnClearData)
        clearData.setOnClickListener{
            clearDataConfirmation()
        }

    }

    //function to configure the seekbar and auto-save switch on Activity onCreate
    private fun configureSettings(){
        speechRate.max = 4 //set the seekbar to have 5 levels
        //set the value of the speech rate seekBar
        speechRate.progress = sharedPref.getInt("speech_rate", 2)
        //set the status of the auto-save switch
        autoSaveSwitch.isChecked = sharedPref.getBoolean("auto_save", false)

    }

    private fun clearDataConfirmation(){
        //Set up the Dialog Alert
        val alert = AlertDialog.Builder(this)

        alert.setTitle("Clear App Data")
        alert.setMessage("This will clear the settings and delete all the images saved by the app")
        //Set up the confirm and cancel buttons
        alert.setPositiveButton("Confirm") { _, _ ->
            clearImages()
            clearSharedPreferences()
            //reset the Activity views
            configureSettings()
        }
        alert.setNegativeButton("Cancel"){ _, _ ->
            //No action
        }
        alert.create().show()
    }

    //function to clear and reset the settings of the app
    private fun clearSharedPreferences(){
        sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }

    //function to delete all the images saved by the app
    private fun clearImages(){
        //set up the path where the images are stored
        val path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + File.separator + "IntakaIsland"
        val customDirectory = File(path)

        //if the directory for the images exists, then delete all the images stored in there
        if (customDirectory.exists() && customDirectory.isDirectory) {
            val files = customDirectory.listFiles()
            if (files != null) {
                for (file in files) {
                    //delete the file
                    file.delete()
                }
            }
        }
    }

}