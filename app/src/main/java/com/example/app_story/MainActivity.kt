package com.example.app_story

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.app_story.data.UserPreference
import com.example.app_story.ui.HomeActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            val userPreference = UserPreference.getInstance(this@MainActivity)
            val token = userPreference.getToken().first()

            if (!token.isNullOrEmpty()) {
                // Token ada, pindah ke HomeActivity
                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                startActivity(intent)
                finish() // Tutup MainActivity agar tidak bisa kembali
            }
        }
    }
}
