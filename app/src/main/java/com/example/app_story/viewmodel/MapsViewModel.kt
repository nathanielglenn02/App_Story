package com.example.app_story.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_story.model.Story
import com.example.app_story.repository.StoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapsViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> get() = _stories

    fun getStoriesWithLocation(token: String) {
        viewModelScope.launch {
            repository.getStoriesWithLocation(token).collect { list ->
                _stories.value = list
            }
        }
    }
}
