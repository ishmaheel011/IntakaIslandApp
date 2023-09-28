package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Calendar
import com.google.android.gms.maps.model.Marker

class MapManager {
    private lateinit var context : Context
    private var map: GoogleMap? = null
    private var deviceMarker: Marker? = null
    private var markerOptions: ArrayList<MarkerOptions> = ArrayList<MarkerOptions>()
    private var deviceWithinBounds: Boolean = false
    val INTAKASOUTHWESTCORNER: LatLng = LatLng(-33.891376, 18.512007)
    val INTAKANORTHEASTCORNER: LatLng = LatLng(-33.884545, 18.520955)
    private var markers : ArrayList<Marker?> = ArrayList<Marker?>()

    /**
     * creates MapManager object
     */
    class MapManager constructor(){

    }

    /**
     * initialises map properties
     * sets bounds
     * place all markers on the map
     */
    fun init(m : GoogleMap?, c: Context){
        map = m
        context = c
        map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
        setMapBounds()
        placeMarkers()
        placeSightingMarkers()
    }

    /**
     * Adds new MarkerOptions object to markerOtions arraylists
     */

    fun addMarkerOption(marker: MarkerOptions){
        markerOptions.add(marker)
    }


    /**
     * returns a string of lines containing details of the markers on the map
     * Each line is in the format : "nameOfSpecies, date, latitude, longitude\n"
     */
    fun markersToString(): String{
        if(markers.size > 0){
            var fileContents = ""
            for(marker in markers){
                if(marker != null) {
                    val dateTime = marker.snippet
                    val speciesName = marker.title
                    val lat = marker.position.latitude
                    val lon = marker.position.longitude
                    val line = "$speciesName,$dateTime,$lat,$lon\n"
                    fileContents = fileContents + line
                }
            }
            return fileContents

        }
        return ""
    }
    /**
     * removes the marker passed in arguments from the database of markers
     */

    fun deleteMarker(deleteMarker : Marker){
        lateinit var d : Marker
        for(marker in markers){
            if(marker != null){
                if(marker.equals(deleteMarker)) {
                    d = marker
                    Log.d("marker", "found")
                }
            }
        }
        markers.remove(d)
        Log.d("marker", "deleted")


    }


    /**
     * places the marker of the mobile device at the coordinates passed in the arguments
     * Sets the marker title to 'you' and the marker icon is a blue circle
     */
    fun placeDeviceMarker(lat: Double, lon: Double){
        val pos = LatLng(lat, lon)
        val marker= MarkerOptions().position(pos).title("you").icon(BitmapDescriptorFactory.fromBitmap(vectorToBitmap(context, R.drawable.blue_circle, 48, 48))).visible(false)
        //add marker
        deviceMarker = map?.addMarker(marker)
    }
    /**
     * moves the marker of the  mobile device to the coordinates given
     */

    fun moveDeviceMarker(lat: Double, lon: Double){
        val pos = LatLng(lat, lon)
        deviceMarker?.setPosition(pos)
        deviceMarker?.isVisible = true
    }

    /**
     * Adds markers on the map for all MarkerOptions objects in the markerOptions arraylist
     * Also adds the reference to the marker in an arraylist called markers
     */
    private fun placeSightingMarkers(){
        for(option in markerOptions){
            val ref = map?.addMarker(option)
            markers.add(ref)
        }
    }

    /**
     * Adds new MarkerOptions object to markerOptions with current date and time and
     * Adds marker to map then saves the reference to it in markers
     */
    fun addNewMarkerAtSightingLocation(speciesName: String, lat: Double, lon: Double){
        //get current date
        val dateTime = getCurrentDateTime()

        val pos = LatLng(lat, lon)
        val marker= MarkerOptions().position(pos).title(speciesName).snippet(dateTime).icon(
            BitmapDescriptorFactory.fromBitmap(vectorToBitmap(context, R.drawable.yellow_flag, 72, 72))).draggable(true)
        //add marker
        markerOptions.add(marker)
        val ref = map?.addMarker(marker)
        markers.add(ref)

    }

    /**
     * converts vector drawble with ID passed to bitmap of the dimensions passed and returns it
     */

    fun vectorToBitmap(context: Context, drawableId: Int, width: Int, height: Int): Bitmap {
        // 1. Get a reference to the vector drawable.
        val drawable: Drawable? = ContextCompat.getDrawable(context, drawableId)

        if (drawable == null) {
            throw IllegalArgumentException("Drawable not found")
        }

        // 2. Create a Bitmap object.
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // 3. Draw the vector drawable onto the Bitmap canvas.
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)

        return bitmap
    }

    /**
     * checks if the location is within the bounds and sets deviceWithinBounds to true if it is, false if isn't
     */
    private fun CheckIfDeviceWithinBounds(pos: Location){
        val lat = pos.latitude
        val lon = pos.longitude
        CheckIfDeviceWithinBounds(lat, lon)
    }

    /**
     * checks if the location is within the bounds and sets deviceWithinBounds to true if it is, false if isn't
    */
    private fun CheckIfDeviceWithinBounds(lat: Double, lon: Double){
        if((lat <= INTAKASOUTHWESTCORNER.latitude) && (lat >= INTAKANORTHEASTCORNER.latitude)){

            if((lon <= INTAKASOUTHWESTCORNER.longitude) && (lon >= INTAKANORTHEASTCORNER.longitude))
            {
                deviceWithinBounds = true
            }

        }
        else{
            deviceWithinBounds = false
        }
    }

    /**
     * gets the current date and time and retuns it
     * the format "date: year/month/day time hour:minute"
     */

    private fun getCurrentDateTime(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Month is zero-based
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return "Date: $year/$month/$day time $hour:$minute"
    }

    /**
     * sets the bounds of the map, the user cannot move the map camera beyond the bounds
     */
    private fun setMapBounds(){
        val INTAKA = LatLngBounds(INTAKASOUTHWESTCORNER, INTAKANORTHEASTCORNER)
        // Constrain the camera target to the Intaka Island bounds.
        // Constrain the camera target to the Intaka Island bounds.
        map?.setLatLngBoundsForCameraTarget(INTAKA)
    }

    /**
     * places markers of the different sites at Intaka Island on the maps
     */
    private fun placeMarkers() {

        // else, add them manually
        addMarkerAtLocation("Lapa","Used for parties, sleepovers, picnics, etc", -33.888554, 18.516289)
        addMarkerAtLocation("Viewing Platform 1","An elevated sightseeing platform",-33.887941, 18.514631)
        addMarkerAtLocation("Viewing Platform 2","An elevated sightseeing platform",-33.888388, 18.515177)
        addMarkerAtLocation("Viewing Platform 3","An elevated sightseeing platform",-33.887556, 18.515015)
        addMarkerAtLocation("Viewing Platform 4","An elevated sightseeing platform",-33.886216, 18.517485)
        addMarkerAtLocation("Viewing Platform 5","An elevated sightseeing platform",-33.888382, 18.517033)
        addMarkerAtLocation("Viewing Platform 6","An elevated sightseeing platform",-33.889277, 18.514661)
        addMarkerAtLocation("Bird Mountain","Bird Mountain overlooks the heronries",-33.888890, 18.516713)
        addMarkerAtLocation("Kingfisher Bird Hide", "A camouflaged shelter to view the Kingfisher from",-33.889733, 18.515006)
        addMarkerAtLocation("Heron Bird Hide", "A camouflaged shelter to view Heronry from",-33.889790, 18.515229)
        addMarkerAtLocation("Educational Garden","Garden with labelled plants for info.",-33.888479, 18.516096)
        addMarkerAtLocation("Heronry", "A man-made breeding structure for Herons",-33.889555, 18.515409)
        addMarkerAtLocation("Dipping Pond","Shallow pond used for collecting life to study",-33.888951, 18.516116)
    }


    /**
     * zooms into passed location on the map
     */
    fun zoomIntoLocation(location: Location)
    {
        zoomIntoLocation(location.latitude, location.longitude)
    }

    /**
     * zooms into passed coordinates on the map
     */
    fun zoomIntoLocation(lat: Double, lon: Double){
        val zoomLevel = 16.0f //This goes up to 21
        val pos = LatLng(lat, lon)

        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, zoomLevel))
    }

    /**
     * Adds default marker at the location with the title
     */
    fun addMarkerAtLocation(markerTitle: String,location: Location)
    {
        addMarkerAtLocation(markerTitle, location.latitude, location.longitude)
    }
    /**
     * Adds default marker at the coordinates with the title
     */

    private fun addMarkerAtLocation(markerTitle: String, lat: Double, lon: Double){
        //customize markers based on names
        val pos = LatLng(lat, lon)
        val marker= MarkerOptions().position(pos).title(markerTitle)
        //add marker
        map?.addMarker(marker)
    }
    /**
     * Adds default marker at the coordinates with the title and snippet
     */
    private fun addMarkerAtLocation(markerTitle: String, snippet: String, lat: Double, lon: Double){
        //customize markers based on names
        val pos = LatLng(lat, lon)
        val marker= MarkerOptions().position(pos).title(markerTitle).snippet(snippet)
        //add marker
        map?.addMarker(marker)
    }



}