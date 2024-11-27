package com.example.app_story.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.app_story.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menggunakan View Binding
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set welcome message atau implementasi lainnya
        binding.textViewWelcome.text = "Selamat Datang di Home Activity!"
    }
}
