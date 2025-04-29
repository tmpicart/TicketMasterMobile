package com.example.ticketmastersearch

import FavoriteManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ViewFavorites : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val favorites = db.collection("favorites")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.favorites_activity)


        val favoriteManager = FavoriteManager()
        val recyclerView = findViewById<RecyclerView>(R.id.favorite_events)
        recyclerView.layoutManager = LinearLayoutManager(this)

        favoriteManager.getFavorites { favoritesList ->
            val adapter = FavoritesAdapter(favoritesList, favoriteManager)
            recyclerView.adapter = adapter
        }
    }

    inner class FavoritesAdapter(private var favoriteList: List<Map<String, Any>>, private val favoriteManager: FavoriteManager) : RecyclerView.Adapter<FavoritesAdapter.MyViewHolder>() {

        inner class MyViewHolder(item: View): RecyclerView.ViewHolder(item){
            val eventName = item.findViewById<TextView>(R.id.event_name)
            val dateTime = item.findViewById<TextView>(R.id.venue_datetime)
            val venueName = item.findViewById<TextView>(R.id.venue_name)
            val linkButton = item.findViewById<Button>(R.id.link_button)
            val favoriteCheck = item.findViewById<CheckBox>(R.id.favorite)

            init {
                linkButton.setOnClickListener { linkClick() }
                favoriteCheck.setOnCheckedChangeListener { _, isChecked ->
                    if (!isChecked) {
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val current = favoriteList[position]
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            userId?.let { uid ->
                                val userFavoritesCollection = favorites.document(uid).collection("userFavorites")
                                val favoriteId = current["id"].toString()
                                userFavoritesCollection.document(favoriteId).delete()
                                val mutableList = favoriteList.toMutableList()
                                mutableList.removeAt(position)
                                favoriteList = mutableList.toList()
                                notifyItemRemoved(position)
                            }
                        }
                    }
                }
            }


            private fun linkClick() {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val current = favoriteList[position]
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(current["url"].toString()))
                    itemView.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.favorite_row_item, parent, false)
            return MyViewHolder(view)
        }

        override fun getItemCount(): Int {
            return favoriteList.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val current = favoriteList[position]
            holder.eventName.text = current["name"].toString()
            holder.dateTime.text = current["date"].toString()
            holder.venueName.text = current["venueName"].toString()
            holder.favoriteCheck.isChecked = true

        }
    }
}
