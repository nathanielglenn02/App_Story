package com.example.app_story.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.app_story.R
import com.example.app_story.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val transitionName = intent.getStringExtra(EXTRA_TRANSITION_NAME)
        binding.ivDetailPhoto.transitionName = transitionName
        val name = intent.getStringExtra(EXTRA_NAME)
        val description = intent.getStringExtra(EXTRA_DESCRIPTION)
        val photoUrl = intent.getStringExtra(EXTRA_PHOTO_URL)
        binding.tvDetailName.text = name
        binding.tvDetailDescription.text = description
        Glide.with(this)
            .load(photoUrl)
            .placeholder(R.drawable.ic_placeholder)
            .into(binding.ivDetailPhoto)
    }

    companion object {
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_DESCRIPTION = "extra_description"
        const val EXTRA_PHOTO_URL = "extra_photo_url"
        const val EXTRA_TRANSITION_NAME = "extra_transition_name"
    }
}
