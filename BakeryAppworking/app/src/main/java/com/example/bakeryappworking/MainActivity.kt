package com.example.bakeryappworking

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import android.os.Build
import android.content.res.Resources
import android.util.Log
import android.view.View
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            try {
                val getStartedBtn = findViewById<Button>(R.id.btn_get_started)
                val signInBtn = findViewById<TextView>(R.id.btn_sign_in)
                val heroBg = findViewById<ImageView>(R.id.hero_bg)
                val fallbackText = findViewById<TextView>(R.id.hero_bg_fallback)

                // Error handling for missing hero_bg drawable
                try {
                    val resId = resources.getIdentifier("hero_bg", "drawable", packageName)
                    if (resId == 0) {
                        heroBg.visibility = View.GONE
                        fallbackText.visibility = View.VISIBLE
                    } else {
                        heroBg.setImageResource(resId)
                        fallbackText.visibility = View.GONE
                    }
                } catch (e: Resources.NotFoundException) {
                    Log.e("MainActivity", "Hero background image not found", e)
                    heroBg.visibility = View.GONE
                    fallbackText.visibility = View.VISIBLE
                }

                // Both buttons should navigate to LoginActivity
                getStartedBtn.setOnClickListener {
                    try {
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error starting LoginActivity", e)
                        Toast.makeText(this, "Error starting login", Toast.LENGTH_SHORT).show()
                    }
                }

                signInBtn.setOnClickListener {
                    try {
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error starting LoginActivity", e)
                        Toast.makeText(this, "Error starting login", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error setting up views", e)
                Toast.makeText(this, "Error initializing app", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Fatal error in onCreate", e)
            Toast.makeText(this, "App initialization failed", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}