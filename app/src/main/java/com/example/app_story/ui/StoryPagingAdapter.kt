package com.example.app_story.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.app_story.R
import com.example.app_story.databinding.ItemStoryBinding
import com.example.app_story.model.Story

class StoryPagingAdapter(
    private val onItemClick: (Story, View, String) -> Unit
) : PagingDataAdapter<Story, StoryPagingAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(story: Story, onItemClick: (Story, View, String) -> Unit) {
            val transitionName = "storyImage_${story.id}"
            binding.ivItemPhoto.transitionName = transitionName

            binding.tvItemName.text = story.name
            Glide.with(binding.ivItemPhoto.context)
                .load(story.photoUrl)
                .placeholder(R.drawable.ic_placeholder)
                .into(binding.ivItemPhoto)

            binding.root.setOnClickListener {
                onItemClick(story, binding.ivItemPhoto, transitionName)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(story, onItemClick)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }
        }
    }
}


