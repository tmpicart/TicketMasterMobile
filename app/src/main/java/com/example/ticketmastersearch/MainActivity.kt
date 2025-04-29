package com.example.ticketmastersearch

import FavoriteManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private val BASE_URL = "https://app.ticketmaster.com/discovery/v2/"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.view_favorites).setOnClickListener {
            val intent = Intent(this, ViewFavorites::class.java)
            startActivity(intent)
        }

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            startRegisterActivity()
        } else {

            findViewById<Button>(R.id.logout_button).setOnClickListener (){
                FirebaseAuth.getInstance().signOut()
                startRegisterActivity()
            }

            val events = ArrayList<Event>()
            val favoriteManager = FavoriteManager()
            val adapter = EventsAdapter(events, favoriteManager, this)
            val recyclerView = findViewById<RecyclerView>(R.id.events)
            recyclerView.adapter = adapter

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val eventSearch = retrofit.create(GetEvents::class.java)

            val btn = findViewById<Button>(R.id.search_button)
            recyclerView.layoutManager = LinearLayoutManager(this)

            btn.setOnClickListener {
                events.clear()
                val keywordView = findViewById<EditText>(R.id.keyword_search)
                val cityView = findViewById<EditText>(R.id.city_search)

                keywordView.hideKeyboard()
                cityView.hideKeyboard()

                val keyword = keywordView.text.toString()
                val city = cityView.text.toString()

                if (keyword.isEmpty() || city.isEmpty()) {
                    if (city.isEmpty()) {
                        showAlert("City Missing", "City cannot be empty. Please enter a location!")
                    }
                    if (keyword.isEmpty()) {
                        showAlert("Search Term Missing", "Search term cannot be empty. Please enter a search term!")
                    }
                } else {
                    eventSearch.getEventInfo(
                        keyword,
                        city,
                        "date,asc",
                        "ticketmaster-key-here"
                    ).enqueue(object : Callback<EventData> {
                        override fun onResponse(
                            call: Call<EventData>,
                            response: Response<EventData>
                        ) {
                            Log.d(TAG, "onResponse: $response")
                            val body = response.body()
                            Log.w(TAG, body.toString())
                            if (body == null) {
                                Log.w(TAG, "Valid response was not received")
                                return
                            }
                            if (body._embedded == null) {
                                findViewById<TextView>(R.id.no_results).visibility = VISIBLE
                                return
                            }
                            findViewById<TextView>(R.id.no_results).visibility = GONE
                            val uniqueEvents = body._embedded.events.distinctBy { it.name }
                            events.addAll(uniqueEvents)
                            favoriteManager.updateFavorites(events, adapter)
                        }

                        override fun onFailure(call: Call<EventData>, t: Throwable) {
                            Log.d(TAG, "onFailure : $t")
                        }
                    })
                }
            }
        }
    }

    private fun View.hideKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as
                InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun showAlert(title: String, message: String) {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle(title)
        alertBuilder.setMessage(message)
        alertBuilder.setIcon(android.R.drawable.ic_delete)
        alertBuilder.setNegativeButton("Okay") { alert, which -> }
        val alert = alertBuilder.create()
        alert.show()
    }

    private fun startRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }
}
