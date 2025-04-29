package com.example.ticketmastersearch

data class EventData(
    val _embedded: Embedded
)

data class Embedded(
    val events: List<Event>
)

data class Event(
    val name: String,
    val dates: Dates,
    val _embedded: EventEmbedded,
    val images: List<Image>,
    val url: String,
    val priceRanges: List<PriceRange>,
    val id : String,
    var isFavorite: Boolean = false // Default value is false
)

data class PriceRange(
    val min: Double,
    val max: Double
)

data class Image(
    val url: String,
    val width: Int,
    val height: Int,
)
data class Dates(
    val start: DateStart
)

data class DateStart(
    val localTime: String,
    val localDate: String
)

data class EventEmbedded(
    val venues: List<Venue>
)

data class Venue(
    val name: String,
    val city: VenueCity,
    val state: VenueState,
    val address: VenueAddress
)

data class VenueCity(
    val name: String
)

data class VenueState(
    val name: String
)

data class VenueAddress(
    val line1: String
)

