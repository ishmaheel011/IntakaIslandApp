package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter for Search Screen RecyclerView
class SearchAdapter(private val dataList: ArrayList<SpeciesClass>): RecyclerView.Adapter<SearchAdapter.ViewHolderClass>() {

    var onItemClick: ((SpeciesClass) -> Unit)? = null

    // binds xml file to view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.species_card, parent, false)
        return ViewHolderClass(itemView)
    }

    override fun getItemCount(): Int {
       return dataList.size
    }

    // sets appropriate species name and image
    // on card
    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = dataList[position]
        holder.speciesImage.setImageResource(currentItem.getImage())
        holder.speciesName.text = currentItem.getName()

        holder.itemView.setOnClickListener{
            onItemClick?.invoke(currentItem)
        }
    }

    // the view holder allows us to access the GUI components on the card
    // and adjust them accordingly
    class ViewHolderClass(itemView: View): RecyclerView.ViewHolder(itemView) {
        val speciesImage: ImageView = itemView.findViewById(R.id.cardImage)
        val speciesName: TextView = itemView.findViewById(R.id.cardName)
    }
}