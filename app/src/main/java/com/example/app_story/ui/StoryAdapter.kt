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
    private val onItemClick: (Story) -> Unit // Callback untuk item klik
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    class StoryViewHolder(
        private val binding: ItemStoryBinding,
        private val onItemClick: (Story) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(story: Story) {
            binding.tvItemName.text = story.name
            Glide.with(binding.ivItemPhoto.context)
                .load(story.photoUrl)
                .placeholder(R.drawable.ic_placeholder) // Placeholder untuk gambar
                .into(binding.ivItemPhoto)

            // Listener klik item
            binding.root.setOnClickListener {
                onItemClick(story)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(stories[position])
    }

    override fun getItemCount(): Int = stories.size

    // Metode untuk memperbarui data cerita
    fun updateStories(newStories: List<Story>) {
        stories = newStories
        notifyDataSetChanged()
    }
}
