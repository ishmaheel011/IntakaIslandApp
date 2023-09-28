package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var navBar: BottomNavigationView
    private lateinit var speciesList: ArrayList<SpeciesClass>
    private val hAdapter: homeAdapter by lazy { homeAdapter() }
    private var numBirds : Int = 0
    private lateinit var birdOfTheDay: SpeciesClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // thread that populates SpeciesClass array from text files
        val runnableDataLoader = DataLoaderThread(this)
        val dataLoaderThread = Thread(runnableDataLoader)
        dataLoaderThread.start()

        // set Toolbar text appropriately
        var toolBar: Toolbar = findViewById(R.id.materialToolbar)
        toolBar.title = getString(R.string.app_name)

        navBar = findViewById(R.id.bottomNavigationView)
        //set focus to home icon
        navBar.selectedItemId = R.id.homeItem


        // thread needs to terminate before assigning variables
        speciesList = arrayListOf()
        dataLoaderThread.join()
        // get list from Thread
        speciesList = runnableDataLoader.getSpecies()
        numBirds = runnableDataLoader.getNumBirds()

        // code to switch to Activities using a navigation bar
        navBar.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.searchItem -> {
                    val intentSearch = Intent(this@MainActivity, Search::class.java)
                    startActivity(intentSearch)
                    // transition left to right
                    this.overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.cameraItem -> {
                    val intentCamera = Intent(this@MainActivity, Scan::class.java)
                    startActivity(intentCamera)
                    // transition left to right
                    this.overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.homeItem -> {
                    // do nothing
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.mapItem -> {
                    val intentMap = Intent(this@MainActivity, MapActivity::class.java)
                    startActivity(intentMap)
                    // transition left to right
                    this.overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
                    return@setOnNavigationItemSelectedListener true
                }

            }
            false
        }

        // going to various toolbar activities when they're selected
        toolBar = findViewById(R.id.materialToolbar)
        toolBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settingsItem -> {
                    val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                    startActivity(intent)
                    // top to bottom
                    this.overridePendingTransition(R.anim.slide_from_top,R.anim.slide_to_bottom)
                    return@setOnMenuItemClickListener true
                }

                R.id.helpItem -> {
                    val intent = Intent(this@MainActivity, HelpActivity::class.java)
                    startActivity(intent)
                    // top to bottom
                    this.overridePendingTransition(R.anim.slide_from_top,R.anim.slide_to_bottom)
                    return@setOnMenuItemClickListener true
                }

                R.id.aboutUsItem -> {
                    val intent = Intent(this@MainActivity, AboutUsActivity::class.java)
                    startActivity(intent)
                    // top to bottom
                    this.overridePendingTransition(R.anim.slide_from_top,R.anim.slide_to_bottom)
                    return@setOnMenuItemClickListener true
                }
            }
            false

        }

        // get bird of the day
        birdOfTheDay = getBirdOfTheDay()

        //populating cards
        runOnUiThread {
            hAdapter.setData(getCards())

            // updating layout accordingly
            findViewById<RecyclerView>(R.id.recyclerView).apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                hasFixedSize()
                this.adapter = hAdapter
            }
        }

    }

    private fun getCards(): List<homeCards> = listOf(
        // all the cards displayed on the home screen

        // Random image of a bird
        homeCards.otherImages(
            otherImage = R.drawable.duck
        ),
        // park information card
        homeCards.parkInfo(info = ""),
        // Random image of a intaka
        homeCards.otherImages(
            otherImage = R.drawable.flyingbird
        ),
        // bird of the day card
        homeCards.birdOfTheDay(
            birdName = birdOfTheDay.getName(),
            birdImage = birdOfTheDay.getImage(),
            birdDescription = birdOfTheDay.getDesc()
        ),
        // Random image of a bird
        homeCards.otherImages(
            otherImage = R.drawable.smallbird
        ),
        // Info Image about Scan Screen
        homeCards.infoCard(
            title = "Image Recognition",
            infoImage = R.drawable.scan_example,
            additionalInfo = "Press the camera icon on the navigation bar and snap a picture of any bird or plant you see at Intaka Island to learn more about it."
        ),
        // Image of intaka
        homeCards.otherImages(
            otherImage = R.drawable.intakapic
        ),
        // add more
    )

    private fun getBirdOfTheDay(): SpeciesClass {
        // we have N birds (~ 114) which is less than 365 days in a year
        // least complex implementation is take currentDayOfYear % numBirds
        // and take the value as the index of that as the bird of the day
        // bonus is that this method automatically accounts for leap years
        val dayOfYear: Int = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        var birdIndex: Int = dayOfYear % numBirds
        // now birdIndex is between 0 and 113 (since N = 114)
        // don't want birds to change in alphabetical order daily (not "random")
        // to "randomise": multiply birdIndex by number coprime to 114 as it
        // is a generator of the group Z_114 and thus no birds will be skipped over
        // (still always generates the same result for a given day)
        val factor = 83
        // this factor need only be changed if N is a multiple of 83 (i.e. 83, 166, 249, etc.)
        birdIndex = (factor*birdIndex) % numBirds

        return speciesList[birdIndex]
    }

}