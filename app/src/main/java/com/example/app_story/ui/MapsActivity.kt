package com.example.app_story.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.app_story.R
import com.example.app_story.databinding.ActivityMapsBinding
import com.example.app_story.network.ApiConfig
import com.example.app_story.repository.StoryRepository
import com.example.app_story.viewmodel.MapsViewModel
import com.example.app_story.viewmodel.MapsViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var mMap: GoogleMap

    private val mapsViewModel: MapsViewModel by viewModels {
        MapsViewModelFactory(StoryRepository(ApiConfig.getApiService()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        loadStoryMarkers()
    }

    private fun loadStoryMarkers() {
        val token = runBlocking { "Bearer ${getToken()}" }
        mapsViewModel.getStoriesWithLocation(token)

        lifecycleScope.launch {
            mapsViewModel.stories.collect { stories ->
                stories.forEach { story ->
                    val latLng = LatLng(story.lat ?: 0.0, story.lon ?: 0.0)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(story.name)
                            .snippet(story.description)
                    )
                }
                // Pindahkan kamera ke lokasi pertama jika ada
                stories.firstOrNull()?.let {
                    val firstLocation = LatLng(it.lat ?: 0.0, it.lon ?: 0.0)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 5f))
                }
            }
        }
    }


    private suspend fun getToken(): String {
        return com.example.app_story.data.UserPreference.getInstance(applicationContext).getToken().first() ?: ""
    }
}
