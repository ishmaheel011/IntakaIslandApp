package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView

// Adapter class used to support all the children
// of the homeCards sealed class (they are non-interactive)
class homeAdapter : RecyclerView.Adapter<homeAdapter.homeViewHolder>() {

    private val adapterData = mutableListOf<homeCards>()

    // adds specified card to Recycler View
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): homeViewHolder {
        val layout = when(viewType) {

            TYPE_PARKINFO -> R.layout.parkinfo_card
            TYPE_BIRDOFTHEDAY -> R.layout.birdoftheday_card
            TYPE_OTHERIMAGES -> R.layout.image_card
            TYPE_INFOCARD -> R.layout.info_card
            TYPE_HELPCARD -> R.layout.help_card
            else -> throw IllegalArgumentException("Invalid Type")

        }

        val view = LayoutInflater.from(parent.context).inflate(layout,parent,false)
        return homeViewHolder(view)
    }

    // binds card in correct position
    override fun onBindViewHolder(holder: homeViewHolder, position: Int) {
        holder.bind(adapterData[position])
    }

    override fun getItemCount(): Int = adapterData.size

    // determines which child of homeCard is being referenced
    override fun getItemViewType(position: Int): Int {
        return when(adapterData[position]){
            is homeCards.parkInfo -> TYPE_PARKINFO
            is homeCards.birdOfTheDay -> TYPE_BIRDOFTHEDAY
            is homeCards.otherImages -> TYPE_OTHERIMAGES
            is homeCards.infoCard -> TYPE_INFOCARD
            is homeCards.helpCard -> TYPE_HELPCARD
        }
    }

    // allows a list of cards to be input
    fun setData(data: List<homeCards>){
        adapterData.apply{
            clear()
            addAll(data)
        }
    }

    // useful constants to differentiate between card types
    companion object{
        private const val TYPE_PARKINFO = 0
        private const val TYPE_BIRDOFTHEDAY = 1
        private const val TYPE_OTHERIMAGES = 2
        private const val TYPE_INFOCARD = 3
        private const val TYPE_HELPCARD = 4

    }

    // the view holder allows us to access the GUI components on each card type
    // and adjust them accordingly
    class homeViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
        private fun bindParkInfo(item : homeCards.parkInfo) {
            // do nothing
        }

        private fun bindBirdOfTheDay(item : homeCards.birdOfTheDay) {
            itemView.findViewById<TextView>(R.id.nameBirdOfTheDay)?.text = item.getName()
            itemView.findViewById<ImageView>(R.id.imgBirdOfTheDay).setImageResource(item.getImage())
            itemView.findViewById<TextView>(R.id.descriptionBirdOfTheDay)?.text = item.getDesc()
        }
        private fun bindOtherImages(item : homeCards.otherImages) {
            itemView.findViewById<ImageView>(R.id.homeImage).setImageResource(item.getImage())
        }
        private fun bindInfoCard(item : homeCards.infoCard) {
            itemView.findViewById<TextView>(R.id.cardTitle)?.text = item.getTitle()
            itemView.findViewById<ImageView>(R.id.cardImage)?.setImageResource(item.getImage())
            itemView.findViewById<TextView>(R.id.cardDescription)?.text = item.getInfo()
        }
        private fun bindHelpCard(item : homeCards.helpCard) {
            itemView.findViewById<TextView>(R.id.helpView)?.text = item.getHelp()
            itemView.findViewById<ImageView>(R.id.helpIcon)?.setImageResource(item.getImage())
            itemView.findViewById<TextView>(R.id.helpTitle)?.text = item.getTitle()
        }

        // binds according to which child of the homeCards class is referenced
        fun bind(hCards: homeCards) {

            when(hCards) {

                is homeCards.parkInfo -> bindParkInfo(hCards)
                is homeCards.birdOfTheDay -> bindBirdOfTheDay(hCards)
                is homeCards.otherImages -> bindOtherImages(hCards)
                is homeCards.infoCard -> bindInfoCard(hCards)
                is homeCards.helpCard -> bindHelpCard(hCards)

            }

        }
    }

}