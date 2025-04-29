package com.example.ticketmastersearch
import FavoriteManager
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat

private const val TAG = "EventsAdapter"

class EventsAdapter(private val events: List<Event>, private val favoriteManager: FavoriteManager, val mainActivity: MainActivity) : RecyclerView.Adapter<EventsAdapter.MyViewHolder>() {

    inner class MyViewHolder(item: View): RecyclerView.ViewHolder(item){
        val eventName = item.findViewById<TextView>(R.id.event_name)
        val dateTime = item.findViewById<TextView>(R.id.venue_datetime)
        val venueName = item.findViewById<TextView>(R.id.venue_name)
        val venueLocation = item.findViewById<TextView>(R.id.venue_location)
        val image = item.findViewById<ImageView>(R.id.event_image)
        val price = item.findViewById<TextView>(R.id.price_range)
        val linkButton = item.findViewById<Button>(R.id.link_button)
        val favoriteCheck = item.findViewById<CheckBox>(R.id.favorite)

        init {
            linkButton.setOnClickListener { linkClick() }
            favoriteCheck.setOnCheckedChangeListener { _, isChecked ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val current = events[position]
                    if (current.isFavorite != favoriteCheck.isChecked) {
                        current.isFavorite = isChecked
                        favoriteManager.toggleFavorite(current, isChecked)
                    }
                }
            }
        }

        private fun linkClick() {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val current = events[position]
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(current.url))
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_row_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val current = events[position]
        holder.eventName.text = current.name
        holder.venueName.text = current._embedded.venues[0].name
        holder.venueLocation.text = "${current._embedded.venues[0].address.line1}, ${current._embedded.venues[0].city.name}, ${current._embedded.venues[0].state.name}"

        var date = current.dates.start.localDate

        val dateParts = date.split("-")
        date = "Date: ${dateParts[1]}/${dateParts[2]}/${dateParts[0]}"

        var time = current.dates.start.localTime
        val oldFormat = SimpleDateFormat("HH:mm")
        val newFormat = SimpleDateFormat("h:mm a")
        val dateTime = oldFormat.parse(time)

        time = dateTime?.let { newFormat.format(it)}.toString()

        holder.dateTime.text = "${date} @ ${time}"

        val bestImage = current.images.maxByOrNull {
            it.width * it.height
        }
        val context = holder.itemView.context
        Glide.with(context)
            .load(bestImage?.url)
            .placeholder(R.drawable.baseline_event_24)
            .into(holder.image)

        if (current.priceRanges != null && current.priceRanges.isNotEmpty()) {
            holder.price.visibility = VISIBLE
            holder.price.text = "Price Range: $${current.priceRanges[0].min} - $${current.priceRanges[0].max}"
        }

        holder.favoriteCheck.isChecked = current.isFavorite
    }
}





