package com.example.app_story.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_story.MainActivity
import com.example.app_story.R
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
        setSupportActionBar(binding.toolbar)
        setupRecyclerView()
        setupFab()
        loadStories()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            UserPreference.getInstance(applicationContext).clearUserData()
            val intent = Intent(this@HomeActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryAdapter(emptyList()) { story, holder ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_NAME, story.name)
                putExtra(DetailActivity.EXTRA_DESCRIPTION, story.description)
                putExtra(DetailActivity.EXTRA_PHOTO_URL, story.photoUrl)
            }

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                holder.binding.ivItemPhoto,
                "storyImage"
            )
            startActivity(intent, options.toBundle())
        }

        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = storyAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddStory.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadStories() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val token = "Bearer ${getToken()}"
                ApiConfig.getApiService().getStories(token).enqueue(object : Callback<StoryResponse> {
                    override fun onResponse(call: Call<StoryResponse>, response: Response<StoryResponse>) {
                        showLoading(false)
                        if (response.isSuccessful) {
                            val stories = response.body()?.listStory ?: emptyList()
                            storyAdapter.updateStories(stories)
                            animateRecyclerView()
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
        return UserPreference.getInstance(applicationContext).getToken().first() ?: ""
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun animateRecyclerView() {
        binding.rvStories.alpha = 0f
        binding.rvStories.animate()
            .alpha(1f)
            .setDuration(1000)
            .setListener(null)
            .start()
    }

}

