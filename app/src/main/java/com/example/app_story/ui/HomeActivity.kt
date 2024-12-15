package com.example.app_story.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
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
import com.example.app_story.repository.StoryRepository
import com.example.app_story.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var storyAdapter: StoryPagingAdapter

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(StoryRepository(ApiConfig.getApiService()))
    }

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
            R.id.action_maps -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
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
        storyAdapter = StoryPagingAdapter { story, sharedElementView, transitionName ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_NAME, story.name)
                putExtra(DetailActivity.EXTRA_DESCRIPTION, story.description)
                putExtra(DetailActivity.EXTRA_PHOTO_URL, story.photoUrl)
                putExtra(DetailActivity.EXTRA_TRANSITION_NAME, transitionName)
            }

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                sharedElementView,
                transitionName
            )
            startActivity(intent, options.toBundle())
        }

        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = storyAdapter.withLoadStateFooter(
                footer = StoryLoadStateAdapter { storyAdapter.retry() }
            )
        }

        // LoadStateListener untuk menampilkan progress bar saat loading
        storyAdapter.addLoadStateListener { loadState ->
            if (loadState.source.refresh is androidx.paging.LoadState.Loading ||
                loadState.source.append is androidx.paging.LoadState.Loading
            ) {
                showLoading(true) // Tampilkan progress bar
            } else {
                showLoading(false) // Sembunyikan progress bar
            }

            // Tangani error jika ada
            val errorState = loadState.source.refresh as? androidx.paging.LoadState.Error
            errorState?.let {
                showToast("Error: ${it.error.localizedMessage}")
            }
        }
    }



    private fun setupFab() {
        binding.fabAddStory.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadStories() {
        lifecycleScope.launch {
            val token = "Bearer ${getToken()}"
            homeViewModel.getStories(token).collectLatest { pagingData ->
                storyAdapter.submitData(pagingData)
            }
        }

        storyAdapter.addLoadStateListener { loadState ->
            // Handle loading untuk refresh atau append
            val isLoading = loadState.source.append is androidx.paging.LoadState.Loading ||
                    loadState.source.refresh is androidx.paging.LoadState.Loading
            showLoading(isLoading)

            // Panggil animasi saat data berhasil dimuat
            if (loadState.source.refresh is androidx.paging.LoadState.NotLoading &&
                loadState.append.endOfPaginationReached) {
                animateRecyclerView()
            }

            // Handle error
            val errorState = loadState.source.append as? androidx.paging.LoadState.Error
            errorState?.let {
                showToast("Error: ${it.error.localizedMessage}")
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

