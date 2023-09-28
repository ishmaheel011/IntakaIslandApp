package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar
import android.graphics.drawable.Drawable
import android.graphics.Canvas
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.model.Marker

class MapActivity : AppCompatActivity(),OnMapReadyCallback{

    private lateinit var navBar: BottomNavigationView
    private lateinit var toolBar: MaterialToolbar
    private var map: GoogleMap? = null
    private val locationRequestCode = 900
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var sightingMarkerAdded: Boolean = false
    private var locationEnabled = false
    private var persistentFileManager = PersistentFileManager()
    private var manager = MapManager()


    /**
     * executed when the activity is started
     * sets listeners for UI components
     * reads persistent file
     * fetches GoogleMap object
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map)

        val toolBar: Toolbar = findViewById(R.id.materialToolbar)
        toolBar.title = getString(R.string.maps)

        //get Google Map object
        val mapFragment = supportFragmentManager.findFragmentById(R.id.maps) as SupportMapFragment
        mapFragment.getMapAsync(this)


        setNavBarListener()
        setToolbarListener()
        setBtnZoomButtonListener()

        //initialize persistent file manager object
        persistentFileManager.init(this, "markers.txt")
        readMarkerOptionsFromFile()

    }

    /**
     * executed when the GoogleMap object is ready
     * starts location updates
     * makes changes to map using MapManger object
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        manager.init(map, this)


        val markerDragListener = object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {
                // Called when a marker starts being dragged.
                // The marker's location can be accessed via getPosition()
                AlertDialog.Builder(this@MapActivity)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this marker?")
                    .setPositiveButton("YES"){_, _->
                        marker.isVisible = false
                        manager.deleteMarker(marker)
                        Log.d("delete", "confirmed")
                    }
                    .setNegativeButton("NO"){_,_->
                    }.show()


            }

            override fun onMarkerDrag(marker: Marker) {
                // Called repeatedly while a marker is being dragged.
                // The marker's location can be accessed via getPosition()
            }

            override fun onMarkerDragEnd(marker: Marker) {
                // Called when a marker has finished being dragged.
                // The marker's location can be accessed via getPosition()
            }
        }
        map?.setOnMarkerDragListener(markerDragListener)



        //zoom into Intaka
        manager.zoomIntoLocation(-33.888059, 18.515822)

        //enable gps device if it's off
        enableLocationDevice()

        //request location updates and move marker to current location
        if(locationEnabled){
            requestLocationUpdates()
        }
    }

    /**
     * reads the content of the persisten file and passes it to the mapManager
     */

    private fun readMarkerOptionsFromFile(){
        val fileContents = persistentFileManager.readFromFile()
        if(!fileContents.equals("")){
            val lines = fileContents.split("\n")
            for(line in lines){
                val items = line.split(",")
                if(items.size==4) {
                    val title = items[0]
                    val snippet = items[1]
                    val lat = items[2].toDouble()
                    val lon = items[3].toDouble()
                    val marker = MarkerOptions().title(title).snippet(snippet).position(LatLng(lat, lon)).icon(BitmapDescriptorFactory.fromBitmap(manager.vectorToBitmap(this, R.drawable.yellow_flag, 72, 72))).draggable(true)
                    manager.addMarkerOption(marker)
                }
            }
            Log.d("persistentmarkeroptions", "added")
        }
    }

    /**
     * Takes saved marker information from mapMananger and write it to persistent file
     */
    private fun writeMarkerOptionsToFile(){
        persistentFileManager.writeToFile(manager.markersToString())
    }

    /**
     * write to persistent file when activity is pauseds
     */
    override fun onPause() {
        writeMarkerOptionsToFile()
        super.onPause()
    }


    /**
     * requests device location updates and moves marker to location upo receival of current location
     * updates location every 3 seconds
     * also used to add custom markers at current location
     */
    private fun requestLocationUpdates(){

        // 1. Check if permissions are granted, if so, request location
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            //create locationProviderClient objet
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            //create locationRequest
            val locationRequest = LocationRequest.Builder(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setIntervalMillis(3000)
                .setMinUpdateIntervalMillis(3000)
                .setMaxUpdates(1000)
                .build()

            //create locationCallback
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    result.locations.lastOrNull()?.let { location ->
                        val lat = location.latitude
                        val lon = location.longitude

                        if(intent.getStringExtra("activity").toString().equals("IndividualSpeciesView")){
                            if (!sightingMarkerAdded){
                                manager.addNewMarkerAtSightingLocation(intent.getStringExtra("sightingName").toString(), lat, lon)
                                manager.zoomIntoLocation(lat, lon)
                                sightingMarkerAdded = true
                            }
                        }
                        else{
                            manager.moveDeviceMarker(lat,lon)
                        }
                    }
                }
            }

            //request location updates
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

        // 2. If if a permission rationale dialog should be shown
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            Toast.makeText(this, "Device location is required to add your marker to the map", Toast.LENGTH_LONG)
                .show()
        }

        // 3. Otherwise, request permission
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            locationRequestCode
        )

    }


    /**
     * sets listener for the default zoom level button
     */
    private fun setBtnZoomButtonListener(){
        val btnZoom = findViewById(R.id.btnFixZoom) as ImageButton
        btnZoom.setOnClickListener(){
            //zoom into Intaka at default zoom level
            manager.zoomIntoLocation(-33.888059, 18.515822)
        }
    }

    //add marker listeners


    /**
     * asks the user to enable the GPS if's off
     * redirects to location settings if user approves it
     */
    fun enableLocationDevice(){
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if(!locationEnabled)
        {
            AlertDialog.Builder(this)
                .setTitle("Enable GPS")
                .setMessage("Your GPS device is needed to show location. Do you want to enable it?")
                .setPositiveButton("YES"){_, _->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
                .setNegativeButton("NO"){_,_->

                }.show()

        }
    }

    /**
     * sets listener for the bottom navigation bar items
     */
    private fun setNavBarListener()
    {
        navBar = findViewById(R.id.bottomNavigationView)
        //set focus to home icon
        navBar.selectedItemId = R.id.mapItem
        // code to switch to Activities using a navigation bar
        navBar.setOnNavigationItemSelectedListener{ item ->
            when (item.itemId) {
                R.id.cameraItem -> {
                    val intentCamera = Intent(this@MapActivity, Scan::class.java)
                    startActivity(intentCamera)
                    // transition right to left
                    this.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.mapItem -> {
                    // do nothing
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.searchItem -> {
                    val intentSearch = Intent(this@MapActivity, Search::class.java)
                    startActivity(intentSearch)
                    // transition left to right
                    this.overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.homeItem -> {
                    val intentHome = Intent(this@MapActivity, MainActivity::class.java)
                    startActivity(intentHome)
                    // transition right to left
                    this.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                    return@setOnNavigationItemSelectedListener true
                }

            }
            false
        }
    }

    /**
     * sets listener for the Toolbar menu items
     */
    private fun setToolbarListener() {

        // going to various toolbar activities when they're selected
        toolBar = findViewById(R.id.materialToolbar)
        toolBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settingsItem -> {
                    val intent = Intent(this@MapActivity, SettingsActivity::class.java)
                    startActivity(intent)
                    // top to bottom
                    this.overridePendingTransition(R.anim.slide_from_top,R.anim.slide_to_bottom)
                    return@setOnMenuItemClickListener true
                }

                R.id.helpItem -> {
                    val intent = Intent(this@MapActivity, HelpActivity::class.java)
                    startActivity(intent)
                    // top to bottom
                    this.overridePendingTransition(R.anim.slide_from_top,R.anim.slide_to_bottom)
                    return@setOnMenuItemClickListener true
                }
                R.id.aboutUsItem -> {
                    val intent = Intent(this@MapActivity, AboutUsActivity::class.java)
                    startActivity(intent)
                    // top to bottom
                    this.overridePendingTransition(R.anim.slide_from_top,R.anim.slide_to_bottom)
                    return@setOnMenuItemClickListener true
                }
            }
            false

        }
    }

}