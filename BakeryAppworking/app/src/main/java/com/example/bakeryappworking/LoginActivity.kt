package com.example.bakeryappworking

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bakeryappworking.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val firebaseManager = FirebaseManager.getInstance()
    private val GOOGLE_SIGN_IN_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Google Sign-In
        firebaseManager.initializeGoogleSignIn(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            
            if (email.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    try {
                        firebaseManager.signIn(email, password)
                        navigateToHome()
                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvSignUp.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            
            if (email.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    try {
                        firebaseManager.signUp(email, password)
                        navigateToHome()
                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, "Sign up failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            startActivityForResult(firebaseManager.getGoogleSignInIntent(), GOOGLE_SIGN_IN_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            lifecycleScope.launch {
                try {
                    firebaseManager.handleGoogleSignInResult(data)
                    navigateToHome()
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
} 