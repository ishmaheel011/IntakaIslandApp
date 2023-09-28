package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.Surface.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.myapplication.databinding.ActivityScanBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread


class Scan : AppCompatActivity() {

    private lateinit var navBar: BottomNavigationView
    private lateinit var viewBinding: ActivityScanBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var capturedImage: Bitmap
    private var imageCapture: ImageCapture? = null
    private var galleryImage: Boolean = false

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runOnUiThread {
            viewBinding = ActivityScanBinding.inflate(layoutInflater)
            setContentView(viewBinding.root)
        }

        if (!checkInternetConnection(this)){
            showInternetConnectionAlert()
            super.onPause()
        }

        permissionsHandler()

        // Set up the listener for capture button
        viewBinding.btnCapture.setOnClickListener {
            //animate the button
            onCapture(viewBinding)
            //prevent multiple capture requests
            it.isEnabled = false
            takePhoto()
        }

        viewBinding.btnGallery.setOnClickListener{
            openGallery()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        // set Toolbar text appropriately
        val toolBar: MaterialToolbar = findViewById(R.id.materialToolbar)
        toolBar.title = getString(R.string.scan)

        // going to various toolbar activities when they're selected
        toolBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settingsItem -> {
                    val intent = Intent(this@Scan, SettingsActivity::class.java)
                    startActivity(intent)
                    // top to bottom
                    this.overridePendingTransition(R.anim.slide_from_top,R.anim.slide_to_bottom)
                    return@setOnMenuItemClickListener true
                }

                R.id.helpItem -> {
                    val intent = Intent(this@Scan, HelpActivity::class.java)
                    startActivity(intent)
                    // top to bottom
                    this.overridePendingTransition(R.anim.slide_from_top,R.anim.slide_to_bottom)
                    return@setOnMenuItemClickListener true
                }
                R.id.aboutUsItem -> {
                    val intent = Intent(this@Scan, AboutUsActivity::class.java)
                    startActivity(intent)
                    // top to bottom
                    this.overridePendingTransition(R.anim.slide_from_top,R.anim.slide_to_bottom)
                    return@setOnMenuItemClickListener true
                }
            }
            false

        }

        navBar = findViewById(R.id.bottomNavigationView)
        //set focus to camera icon
        navBar.selectedItemId = R.id.cameraItem
        // code to switch to Activities using a navigation bar
        navBar.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.searchItem -> {
                    val intentSearch = Intent(this@Scan, Search::class.java)
                    startActivity(intentSearch)
                    // transition left to right
                    this.overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.cameraItem -> {
                    // do nothing
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.homeItem -> {
                    val intentHome = Intent(this@Scan, MainActivity::class.java)
                    startActivity(intentHome)
                    // transition right to left
                    this.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.mapItem -> {
                    val intentMap = Intent(this@Scan, MapActivity::class.java)
                    startActivity(intentMap)
                    // transition left to right
                    this.overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
                    return@setOnNavigationItemSelectedListener true
                }

            }
            false

        }
    }

    /*
    Function to handle the permission request responses, either start the camera if all permissions
    have been granted otherwise, request the permissions
     */
    private fun permissionsHandler(){
        // Request permissions
        if (allPermissionsGranted()) {
            // run camera process on another thread to reduce lag
            thread {startCamera() }
        } else {
            requestPermissions()
        }
    }

    /*
    Function to check all the permissions required have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    /*
    Function overridden from the CameraX API to be invoked when the camera closes and its resources are cleaned up
     */
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    /*
    Handles the Image Capture use case of the camera, passes the captured image to the scanPreview activity
     */
    private fun takePhoto() {

        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this@Scan),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {

                    capturedImage = image.planes[0].buffer.toBitmap()
                    image.close()
                    galleryImage = false
                    saveAndDisplayImage(capturedImage)

                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("Image Capture", "Photo capture failed: ${exc.message}", exc)
                }
            }
        )
    }

    /*
    Function is used to initialize the Camera, set up the parameters needed to run the Camera use cases,
    preview and image capture, set up the Back(Rear) camera to be the default camera in use. The parameters include
    the ProcessCameraProvider which binds the lifecycle of the camera to the lifecycle owner. Sets the preview use case
    to the Preview view as the surface provider.
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.scanPreview.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().setTargetRotation(ROTATION_180).build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e("Camera Initialization", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    /*
    The companion object defines the required permissions in an array
     */
    companion object {
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).apply {
            }.toTypedArray()
    }

    /*
    ActivityResultLauncher requests multiple permissions from REQUIRED_PERMISSIONS and handles the results
    of the requests. If all the permissions have been granted then the camera is started, otherwise the
    permission dialog is invoked.
     */
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }

            if (!permissionGranted) {
                permissionDialog()
            } else {
                startCamera()
            }
        }

    /*
    Helper function to launch the ActivityResultLauncher to request the required permissions
     */
    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    // Extension function to convert Image to Bitmaps
    private fun ByteBuffer.toBitmap(): Bitmap {
        rewind()
        val bytes = ByteArray(remaining())
        get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    /*
    Function to animate the capture button to be responsive when it is pressed
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun onCapture(viewBinding: ActivityScanBinding){

        viewBinding.btnCapture.setOnTouchListener { _, event ->
            when (event.action) {
                //Upon pressing the button
                MotionEvent.ACTION_DOWN -> {
                    viewBinding.btnCapture.setImageResource(R.drawable.ic_shutter_pressed)
                }
                MotionEvent.ACTION_UP -> {
                    //Upon releasing the button
                    viewBinding.btnCapture.setImageResource(R.drawable.ic_shutter_normal)
                }
            }
            false

        }
    }

    /*
    The function is used to check the current internet connection status (Wifi or Cellular Network) of the device
     */
    private fun checkInternetConnection(context: Context): Boolean{

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(network)

        // Check if network is connected through WI-FI or Cellular network and has internet capability
        if (capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            ))
        ) {
            return true
        }
        return false
    }

    /*
    function to show an alert Dialog if the device has no internet connection. The alert dialog has two buttons, RETRY and CANCEL.
    RETRY button checks the internet connection status again
    CANCEL button aborts the Scan feature and returns to the previous activity
     */
    private fun showInternetConnectionAlert(){

        AlertDialog.Builder(this)
            .setTitle("Internet Connection")
            .setMessage("This feature requires an internet connection. Please check your internet connection and try again")
            .setPositiveButton("RETRY"){_, _->
                //check internet connection again
                if (!checkInternetConnection(this)) {
                    showInternetConnectionAlert()
                }
            }
            .setNegativeButton("CANCEL"){_,_->
                finish() // go back to the previous activity
            }.show()

    }

    /*
    Function to alert the user that the required permission for the feature have not been granted
    and offers them an option to re-request the permission(s)
     */
    private fun permissionDialog(){
        AlertDialog.Builder(this).setMessage("This feature requires permission to access the camera and storage. You can enable these in App Settings")
            .setPositiveButton("ACCEPT PERMISSIONS"){_, _->
                permissionsHandler()
            }
            .setNegativeButton("CANCEL"){dialog,_->
                dialog.dismiss()// close the dialog
                // finish() //go back to the previous activity
            }.show()

    }

    /*
    Open the device gallery to pick an image to be submitted for scanning
     */
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    /*
    Handle the response from the Gallery intent launched by openGallery. Once the image is selected,
    it is converted to a Bitmap so it can be displayed on the scanPreview Activity
     */
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = result.data?.data
            //convert the Uri to a bitmap
            val selectedImage = createBitmapFromUri(this, selectedImageUri!!)
            galleryImage = true
            saveAndDisplayImage(selectedImage!!)

        }
    }

    /*
    Extension function to create a Bitmap from an image Uri
     */
    private fun createBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            return BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return null
    }

    /*
    Function to save the bitmap to the singleton and start the scanPreview Activity
     */
    private fun saveAndDisplayImage(imageBitmap: Bitmap){
        // save bitmap to singleton
        CapturedImage.bitmap = imageBitmap

        val intentPreview = Intent(this@Scan, scanPreview::class.java)
        intentPreview.putExtra("galleryImage", galleryImage)
        startActivity(intentPreview)
        // zoom-in animation
        this@Scan.overridePendingTransition(R.anim.zoom_in,R.anim.static_animation)
    }
}
