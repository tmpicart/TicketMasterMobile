package com.example.ticketmastersearch

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.firebase.ui.auth.IdpResponse

class RegisterActivity : AppCompatActivity() {

    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)



        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val signActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    val response = IdpResponse.fromResultIntent(result.data)
                    if (response == null) {
                        Log.d(TAG, "onActivityResult: the user has cancelled the sign in request")
                    } else {
                        Log.e(TAG, "onActivityResult: ${response.error?.errorCode}")
                    }
                }
            }
            findViewById<Button>(R.id.login_button).setOnClickListener {
                val providers = arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build()
                )

                val signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setIsSmartLockEnabled(false)
                    .build()

                signActivityLauncher.launch(signInIntent)
            }
        }
    }
}
