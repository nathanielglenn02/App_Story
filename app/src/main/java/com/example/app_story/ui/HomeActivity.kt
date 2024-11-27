package com.example.app_story.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_story.data.UserPreference
import com.example.app_story.databinding.ActivityHomeBinding
import com.example.app_story.model.StoryResponse
import com.example.app_story.network.ApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var storyAdapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi RecyclerView dan tombol tambah cerita
        setupRecyclerView()
//        setupFab()

        // Memuat data cerita dari API
        loadStories()
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryAdapter(emptyList()) { story ->
            // Pindah ke DetailActivity dengan data cerita
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_NAME, story.name)
                putExtra(DetailActivity.EXTRA_DESCRIPTION, story.description)
                putExtra(DetailActivity.EXTRA_PHOTO_URL, story.photoUrl)
            }
            startActivity(intent)
        }

        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = storyAdapter
        }
    }

//    private fun setupFab() {
//        // Tombol tambah cerita
//        binding.fabAddStory.setOnClickListener {
//            val intent = Intent(this, AddStoryActivity::class.java)
//            startActivity(intent)
//        }
//    }

    private fun loadStories() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                // Ambil token dari DataStore secara asynchronous
                val token = "Bearer ${getToken()}"

                // Panggil API untuk mendapatkan daftar cerita
                ApiConfig.getApiService().getStories(token).enqueue(object : Callback<StoryResponse> {
                    override fun onResponse(call: Call<StoryResponse>, response: Response<StoryResponse>) {
                        showLoading(false)
                        if (response.isSuccessful) {
                            val stories = response.body()?.listStory ?: emptyList()
                            storyAdapter.updateStories(stories)
                        } else {
                            showToast("Gagal memuat data: ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                        showLoading(false)
                        showToast("Error: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                showLoading(false)
                showToast("Error: ${e.message}")
            }
        }
    }

    private suspend fun getToken(): String {
        // Mengambil token dari DataStore
        return UserPreference.getInstance(applicationContext).getToken().first() ?: ""
    }

    private fun showLoading(isLoading: Boolean) {
        // Menampilkan atau menyembunyikan ProgressBar
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        // Menampilkan pesan Toast
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
