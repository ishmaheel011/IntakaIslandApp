@file:Suppress("ClassName")

package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class scanPreview : AppCompatActivity() {

    private var progressDialog: ProgressBar? = null
    private var capturedImage: Bitmap? = null
    private lateinit var discard:ImageButton
    private lateinit var scan: ImageButton
    private lateinit var save: ImageButton
    private lateinit var speciesList: ArrayList<SpeciesClass>
    private lateinit var nameSpeciesList: ArrayList<String>
    private lateinit var returnedSpeciesName: String
    private lateinit var imageRecognition: ImageRecognitionService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_preview)
        // thread that populates SpeciesClass array from text files
        val runnableDataLoader = DataLoaderThread(this)
        val dataLoaderThread = Thread(runnableDataLoader)
        dataLoaderThread.start()

        val galleryImage = intent.getBooleanExtra("galleryImage", true)

        //Display the image
        val imageView : ImageView= findViewById(R.id.imagePreview)
        save = findViewById(R.id.btnSaveImage)

        capturedImage = CapturedImage.bitmap
        if (capturedImage != null){
            imageView.setImageBitmap(capturedImage)
            imageView.rotation = 90f
            //if auto-save ON then save image
            val sharedPref: SharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)
            if ((sharedPref.getBoolean("auto_save", false)) && !galleryImage){
                save.isEnabled = false
                save.isVisible = false

                saveImage(capturedImage)
            }
        }else{
            return
        }

        save.setOnClickListener{
            saveImage(capturedImage!!)
            Toast.makeText(this, "Image Saved", Toast.LENGTH_SHORT).show()
        }

        discard = findViewById(R.id.btnDiscard)

        discard.setOnClickListener{
            // return to scan activity
            val intentScan = Intent(this@scanPreview, Scan::class.java)
            startActivity(intentScan)
            // zoom-out animation
            overridePendingTransition(R.anim.static_animation,R.anim.zoom_out)
        }

        // thread needs to terminate before assigning variables
        speciesList = arrayListOf()
        nameSpeciesList = arrayListOf()
        dataLoaderThread.join()
        // get list from Thread
        speciesList = runnableDataLoader.getSpecies()
        // get names from each object in speciesList
        for(species in speciesList){
            nameSpeciesList.add(species.getName())
        }

        // defining progressBar
        progressDialog = findViewById(R.id.progressBar)

        //Save button
        scan = findViewById(R.id.btnScan)
        scan.setOnClickListener{
            progressDialog?.visibility = View.VISIBLE

            // use a coroutine to run Image Recognition in the background
            GlobalScope.launch(start = CoroutineStart.DEFAULT) {
                // in the background
                imageRecognition = ImageRecognitionService(this@scanPreview, nameSpeciesList)
                returnedSpeciesName = imageRecognition.getSpecies()
                Log.d("Final result", returnedSpeciesName)

            }.invokeOnCompletion {
                // when the coroutine is done

                if (returnedSpeciesName == getString(R.string.error)){
                    // display error - species cannot be found
                    runOnUiThread {
                        progressDialog?.visibility = View.GONE
                        Toast.makeText(baseContext,
                            "Sorry the species could not be identified",
                            Toast.LENGTH_LONG).show() }
                    //contentResolver.delete(Uri.parse(imagePath), null, null)
                    val intentScan = Intent(this@scanPreview, Scan::class.java)
                    startActivity(intentScan)
                    // zoom-out animation
                    overridePendingTransition(R.anim.static_animation,R.anim.zoom_out)
                } else{
                    runOnUiThread {progressDialog?.visibility = View.GONE }
                    // makes the animation less abrupt
                    Thread.sleep(100)
                    // find which species object is associated with result
                    val speciesResult = getSpeciesObject(returnedSpeciesName)
                    // go to detailed image view
                    val detailViewIntent = Intent(this@scanPreview, individualSpeciesView::class.java)
                    // sending species class item to detail view activity
                    detailViewIntent.putExtra("species", speciesResult)
                    detailViewIntent.putExtra("returnActivity",getString(R.string.scan))
                    startActivity(detailViewIntent)
                    // zoom-in animation
                    this.overridePendingTransition(R.anim.zoom_in,R.anim.static_animation)
                }
            }

        }

    }

    // helper function to get a species object from its name
    private fun getSpeciesObject(name: String): SpeciesClass? {
        for (species in speciesList){
            if (species.getName().lowercase() == name.lowercase()){
                return species
            }
        }
        return null
    }

    /*

     */
    private fun saveImage(image: Bitmap?){
        //Since Android 11 and above have problems with handling scoped storage restrictions, we used the MediaStore API

        //interact with MediaStore content provider
        val resolver = contentResolver
        val contentValues = ContentValues()
        //Set the image values
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, generateFileName())
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

        // Set the image's relative path within the Pictures directory
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES + "/IntakaIsland")

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        var outputStream: OutputStream? = null
        try{
            outputStream = resolver.openOutputStream(imageUri!!)
            Log.e("Image Save", "${imageUri.path}")
            //convert the image to a JPEG
            image?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            Log.d("Image Save", "Image saved successfully to: $imageUri")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Image Save", "Error saving image: ${e.message}")
        } finally {
            try {
                outputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    //function to generate the name for the saved image using the date and time
    private fun generateFileName(): String{
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }


}