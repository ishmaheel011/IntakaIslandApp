package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread

// helper class to identify a picture of a species using
// Bing's Visual Search API
class ImageRecognitionService(private var context: Context, private var listOfSpecies: ArrayList<String>) {

    private var capturedImage: Bitmap? = CapturedImage.bitmap
    // if nothing changes species an error is returned
    private var resultSpecies: String = context.getString(R.string.error)
    private lateinit var namesList: ArrayList<String>
    private lateinit var response: Response

    // Function used to send a HTTP request to Bing's Visual Search API
    // with the image we want it to analyse and identify.
    // The HTTP response is a JSON string and is stripped apart
    // so that we can extract the names of the photos it returns
    // (i.e. photos of similar species)
    // These names are then compared against our known species' names
    // if there is a match its name is returned
    // if there is no match an error is returned
    private fun recognise() {
        val client = OkHttpClient()

        val subscriptionKey = context.getString(R.string.azure_api_key)
        val url = context.getString(R.string.visual_search_api_endpoint)

        val image = bitmapToByteArray(capturedImage!!, Bitmap.CompressFormat.JPEG, 85)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "image.jpeg", image.toRequestBody("image/jpeg".toMediaTypeOrNull()))
            .build()

        val request = Request.Builder()
            .url(url)
            .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
            .post(requestBody)
            .build()

        // Run as a thread to appease compiler.
        // .join() is called to make sure it terminates
        // before any other activity can attempt to access
        // the results
        thread { response = client.newCall(request).execute()
            val body = response.body?.string()
            if (body != null) {
                Log.d("Response", body)
            }
            val namesList = body?.let { getResults(it) }

            if (namesList != null) {
                // see results in Logcat
                for (name in namesList) {
                    Log.wtf("Results","Name: $name")
                }
                resultSpecies = determineSpecies(namesList, listOfSpecies)
            } }.join()

    }

    // Return result to calling Activity
    fun getSpecies(): String {
        this.recognise()
        Log.d("Result", this.resultSpecies)
        return this.resultSpecies
    }

    // helper function to process JSON http response
    // breaks the response down by JSON headers and subheaders
    // in order to extract all values in "name" fields
    // for a VisualSearch or PagesIncluding type only
    private fun getResults(body: String) : ArrayList<String>? {
        try {
            val gson = Gson()
            val responseJson = gson.fromJson(body, JsonObject::class.java)
            val tagsArray: JsonArray = responseJson.getAsJsonArray("tags")

            namesList = ArrayList<String>() // Initialize the ArrayList

            // Loop through each tag
            for (tagElement in tagsArray) {
                val tagObject: JsonObject = tagElement.asJsonObject

                // Extract actions array
                val actionsArray: JsonArray = tagObject.getAsJsonArray("actions")

                // Loop through actions array
                for (actionElement in actionsArray) {
                    val actionObject: JsonObject = actionElement.asJsonObject
                    val actionType: String = actionObject.get("actionType").asString

                    // only want results from VisualSearch or that exist on Bing already
                    if (actionType == "VisualSearch" || actionType == "PagesIncluding") {
                        // object is empty so it isn't needed -> go to next iteration
                        if (actionObject.getAsJsonObject("data") == null){continue}
                        // Extract webSearchUrl and name from data
                        val dataObject: JsonObject = actionObject.getAsJsonObject("data")
                        val valueArray: JsonArray = dataObject.getAsJsonArray("value")
                        for (valueElement in valueArray) {
                            val valueObject: JsonObject = valueElement.asJsonObject
                            val name: String = valueObject.get("name").asString

                            // Add the name to the ArrayList
                            namesList.add(name)
                        }
                    }
                }
            }

            // Now all the names are in the ArrayList
            return namesList
        } catch (e: Exception) {
            Log.e("Error", "getResults in IR Thread returned an error")
        }
        // in case there's an error
        return null
    }

    // helper function to determine the species from the list of results generated by getResults()
    private fun determineSpecies(namesList: ArrayList<String>, listOfSpecies: ArrayList<String>): String {
        // catch any potential crashes
        if (listOfSpecies.size == 0){return context.getString(R.string.error)}

        // primary check: names match
        for (names in namesList){
            for (species in listOfSpecies){
                // returns first species that matches results
                if (names.lowercase().contains(species, true)){
                    return species
                }
            }
        }

        // can't find a match (will display an error message on detailed view screen)
        return context.getString(R.string.error)
    }

    // helper function to convert bitmap to byte array form
    private fun bitmapToByteArray(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(format, quality, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }


}