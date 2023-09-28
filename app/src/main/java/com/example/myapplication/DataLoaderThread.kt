package com.example.myapplication

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.Log
import java.io.DataInputStream
import java.io.IOException
import java.util.Scanner

// Thread to load bird & plant data in text files into an array
// this array is then sent to the search and home screens where it can be used
class DataLoaderThread(private var context: Context) : Runnable {

    private var res: Int = 0
    private lateinit var imageList: ArrayList<Int>
    private lateinit var nameList: ArrayList<String>
    private lateinit var descriptionList: ArrayList<String>
    private lateinit var speciesList:ArrayList<SpeciesClass>
    private var numBirds: Int = 0
    private var numPlants: Int = 0

    override fun run() {
        // initialising arrays
        imageList = arrayListOf<Int>()
        nameList = arrayListOf<String>()
        descriptionList = arrayListOf<String>()
        speciesList = arrayListOf<SpeciesClass>()

        // populate arrays
        getData()
    }

    // get access to final list from main activity
    // only allow access from there to ensure thread has completed when referenced
    fun getSpecies(): ArrayList<SpeciesClass> {
        return this.speciesList
    }

    // get number of lines in file <-> number of species (both birds & plants) at Intaka
    fun getNumSpecies(): Int{
        return this.numBirds + this.numPlants
    }

    fun getNumPlants(): Int{
        return this.numPlants
    }

    fun getNumBirds(): Int{
        return this.numBirds
    }

    private fun getData() {
        // reading data into arrays using file
        populateArrays(context.getString(R.string.bird_file),context.getString(R.string.bird))
        populateArrays(context.getString(R.string.plant_file),context.getString(R.string.plant))

        // adding each species' image and name to an Array
        for (i in imageList.indices) {
            val speciesClass = SpeciesClass(imageList[i], nameList[i], descriptionList[i])
            speciesList.add(speciesClass)
        }
    }

    // populates imageList, nameList, and descriptionList arrays (depends on bird or plant file)
    private fun populateArrays(filename: String, type: String): Boolean{
        // text files are stored in assets folder
        val textFileStream: DataInputStream = DataInputStream(context.assets.open(String.format(filename)))

        try{
            // conventional read from file as in Java
            val sc = Scanner(textFileStream)
            while (sc.hasNextLine()){
                val line = sc.nextLine()
                val lines = line.split("#")

                // update arrays
                nameList.add(lines[0])
                descriptionList.add(lines[1])


                var imageName = convertToImageName(lines[0])
                res = context.resources.getIdentifier(imageName, "drawable", context.packageName)
                if (type == context.getString(R.string.bird)){
                    numBirds++
                    // comment out when done
                    //res = context.resources.getIdentifier("bird1", "drawable", context.packageName)
                    imageList.add(res)
                } else {
                    numPlants++
                    // comment out when done
                    //res = context.resources.getIdentifier("plant1", "drawable", context.packageName)
                    imageList.add(res)
                }

            }

            sc.close()
        } catch (e: IOException) {
            // caught error so information won't be added to screen (app may crash as well)
        }
        // was successful
        return true

    }

    // helper method
    private fun convertToImageName(name: String): String{
        var result = name.lowercase()
        result = result.replace("""[ '.)(-]""".toRegex(), "_")
        return result
    }
}