package com.example.bakeryappworking

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class BakeryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Enable offline persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
} 