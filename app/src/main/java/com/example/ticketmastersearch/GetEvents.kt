package com.example.ticketmastersearch
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetEvents {

    @GET("events.json")
    fun getEventInfo(
        @Query("keyword") keyword: String,
        @Query("city") city: String,
        @Query("sort", encoded = true) sort: String,
        @Query("apikey") apikey: String): Call<EventData>

}