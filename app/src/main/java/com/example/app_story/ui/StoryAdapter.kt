package com.example.app_story.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.app_story.R
import com.example.app_story.databinding.ItemStoryBinding
import com.example.app_story.model.Story

class StoryAdapter(
    private var stories: List<Story>,
    private val onItemClick: (Story, StoryViewHolder) -> Unit
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    class StoryViewHolder(
        val binding: ItemStoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(story: Story, onItemClick: (Story, StoryViewHolder) -> Unit) {
            binding.tvItemName.text = story.name
            Glide.with(binding.ivItemPhoto.context)
                .load(story.photoUrl)
                .placeholder(R.drawable.ic_placeholder)
                .into(binding.ivItemPhoto)

            binding.root.setOnClickListener {
                onItemClick(story, this)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(stories[position], onItemClick)
    }

    override fun getItemCount(): Int = stories.size

    fun updateStories(newStories: List<Story>) {
        stories = newStories
        notifyDataSetChanged()
    }
}

