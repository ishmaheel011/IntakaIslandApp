package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class Search : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var speciesList:ArrayList<SpeciesClass>
    private lateinit var unsortedList:ArrayList<SpeciesClass>
    private lateinit var colours: ArrayList<String>
    private lateinit var myAdapter: SearchAdapter
    private lateinit var searchBar: SearchView
    private lateinit var searchList: ArrayList<SpeciesClass>
    private lateinit var navBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // setting colours array
        colours = arrayListOf("blue","brown","green","black","white","pink","purple","grey","yellow","olive","orange")

        // set Toolbar text appropriately
        var toolBar: Toolbar = findViewById(R.id.materialToolbar)
        toolBar.title = getString(R.string.search_by_text)

        // thread that populates SpeciesClass array from text files
        val runnableDataLoader : DataLoaderThread = DataLoaderThread(this)
        val dataLoaderThread = Thread(runnableDataLoader)
        dataLoaderThread.start()

        // creating recyclerView and formatting its layout
        recyclerView = findViewById(R.id.recyclerView)
        searchBar = findViewById(R.id.searchBar)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        // populating SpeciesList and searchList
        speciesList = arrayListOf<SpeciesClass>()
        searchList = arrayListOf<SpeciesClass>()
        unsortedList = arrayListOf<SpeciesClass>()

        dataLoaderThread.join()
        // get list from Thread
        unsortedList = runnableDataLoader.getSpecies()
        speciesList = unsortedList.sortedBy{ it.getName() }.toCollection(ArrayList())

        runOnUiThread{
            // updating the search screen view
            searchList.addAll(speciesList)
            recyclerView.adapter = SearchAdapter(searchList)
        }

        // getting search bar ready and setting a listener
        searchBar.clearFocus()
        searchBar.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchBar.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchList.clear()
                // searching for entered searchTerm in nameList
                val searchTerm = newText!!.lowercase(Locale.getDefault())
                if (searchTerm.isNotEmpty()){
                    speciesList.forEach {
                        // primary check: check if species matches searchTerm
                        if (it.getName().lowercase(Locale.getDefault()).contains(searchTerm)) {
                            searchList.add(it)
                        } else {
                            // secondary check: check if species matches colour from their description
                            for (word in it.getDesc().split("""[ .,-]""".toRegex())){
                                if (colours.contains(word.lowercase()) && (word.lowercase() == searchTerm)){
                                    Log.d("Word", word)
                                    searchList.add(it)
                                }
                            }
                        }

                    }
                    recyclerView.adapter!!.notifyDataSetChanged()
                } else {
                    // update list accordingly
                    runOnUiThread {
                        // if searchBar is empty show all species cards
                        searchList.clear()
                        searchList.addAll(speciesList)
                        recyclerView.adapter!!.notifyDataSetChanged()
                    }
                }
                return false
            }


        })

        // using adapter to switch to appropriate detailed view when card is pressed
        myAdapter = SearchAdapter(searchList)
        recyclerView.adapter = myAdapter

        myAdapter.onItemClick = {
            val detailViewIntent = Intent(this, individualSpeciesView::class.java)
            // sending species class item to detail view activity
            detailViewIntent.putExtra("species", it)
            detailViewIntent.putExtra("returnActivity",getString(R.string.search_by_text))
            startActivity(detailViewIntent)
            // zoom-in animation
            this.overridePendingTransition(R.anim.zoom_in,R.anim.static_animation)
        }

        navBar = findViewById(R.id.bottomNavigationView)
        // set focus to search icon
        navBar.selectedItemId = R.id.searchItem
        // code to switch to Activities using a navigation bar
        navBar.setOnNavigationItemSelectedListener{ item ->
            when (item.itemId) {
                R.id.searchItem -> {
                    // do nothing
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.homeItem -> {
                    val intentHome = Intent(this@Search, MainActivity::class.java)
                    startActivity(intentHome)
                    // transition right to left
                    this.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.cameraItem -> {
                    val intentCamera = Intent(this@Search, Scan::class.java)
                    startActivity(intentCamera)
                    // transition right to left
                    this.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.mapItem -> {
                    val intentMap = Intent(this@Search, MapActivity::class.java)
                    startActivity(intentMap)
                    // transition right to left
                    this.overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
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
                    val intent = Intent(this@Search, SettingsActivity::class.java)
                    startActivity(intent)
                    // top to bottom
                    this.overridePendingTransition(R.anim.slide_from_top,R.anim.slide_to_bottom)
                    return@setOnMenuItemClickListener true
                }

                R.id.helpItem -> {
                    val intent = Intent(this@Search, HelpActivity::class.java)
                    startActivity(intent)
                    // top to bottom
                    this.overridePendingTransition(R.anim.slide_from_top,R.anim.slide_to_bottom)
                    return@setOnMenuItemClickListener true
                }

                R.id.aboutUsItem -> {
                    val intent = Intent(this@Search, AboutUsActivity::class.java)
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