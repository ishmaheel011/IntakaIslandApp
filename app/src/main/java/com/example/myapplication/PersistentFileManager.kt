package com.example.myapplication

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class PersistentFileManager {
    private lateinit var context: Context
    private lateinit var fileName: String

    /**
     * creates PersistentFileManager object
     */
    class PersistentFileManager constructor(){
    }

    /**
     * initialises fields
     */
    fun init(c:Context, f:String){
        context = c
        fileName = f
    }

    /**
     * creates persistent file in app's private storage
     */
    fun createPersistentFile(){
        val File = File(context.filesDir, fileName)
        Log.d("persistentfile", "created")
    }

    /**
     * writes the passed string to the persistent file
     */
    fun writeToFile(content: String){
        val outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        outputStream.write(content.toByteArray())
        outputStream.close()
        Log.d("persistentfile", "writtento")
    }

    /**
     * reads the content of the persistent file ands returns a string containing it
     */
    fun readFromFile(): String{
        val file = File(context.filesDir, fileName)
        Log.d("persistentpath", file.toString())
        if (file.exists()){
            Log.d("persistentfile", "exists")
            val text = file.readText()
            return text
        }
        else{
            Log.d("persistentfile", "DNE")
        }
        return ""
    }
}