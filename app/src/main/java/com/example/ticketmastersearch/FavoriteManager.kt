
import android.util.Log
import android.view.View
import com.example.ticketmastersearch.Event
import com.example.ticketmastersearch.EventsAdapter
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FavoriteManager {

    private val db = FirebaseFirestore.getInstance()
    private val favorites = db.collection("favorites")
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val userId = currentUser?.uid

    fun toggleFavorite(event: Event, isFavorite: Boolean) {

        if (userId != null) {
            val userFavoritesCollection = favorites.document(userId).collection("userFavorites")
            if (isFavorite) {
                val favoriteEvent = hashMapOf(
                    "id" to event.id,
                    "name" to event.name,
                    "date" to event.dates.start.localDate,
                    "venueName" to event._embedded.venues[0].name,
                    "url" to event.url
                )
                userFavoritesCollection.document(event.id)
                    .set(favoriteEvent)
            } else {
                userFavoritesCollection.document(event.id)
                    .delete()
            }
        }
    }

    fun updateFavorites(events: List<Event>, adapter: EventsAdapter) {

        if (userId != null) {
            val userFavorites = favorites.document(userId).collection("userFavorites")
            userFavorites.get().addOnSuccessListener { favoriteEvents ->
                val favoriteEventsIds = favoriteEvents.documents.map { it.id }
                for (event in events) {
                    event.isFavorite = favoriteEventsIds.contains(event.id)
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun getFavorites(callback: (List<Map<String, Any>>) -> Unit) {

        if (userId != null) {
            val userFavorites = favorites.document(userId).collection("userFavorites")
            userFavorites.get()
                .addOnSuccessListener { favoriteEvents ->
                    val favoritesList = favoriteEvents.documents.mapNotNull { it.data }
                    Log.d("getFavorites", "Favorites fetched: $favoritesList")
                    callback(favoritesList)
                }
                .addOnFailureListener {
                    Log.e("getFavorites", "Failed to fetch favorites", it)
                    callback(emptyList())
                }
        } else {
            Log.e("getFavorites", "User ID is null")
            callback(emptyList())
        }
    }
}