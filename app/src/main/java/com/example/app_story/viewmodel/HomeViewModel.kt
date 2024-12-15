package com.example.app_story.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.app_story.model.Story
import com.example.app_story.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class HomeViewModel(private val repository: StoryRepository) : ViewModel() {

    private val viewModelScope = CoroutineScope(Dispatchers.IO)

    fun getStories(token: String): Flow<PagingData<Story>> {
        return repository.getStories(token).cachedIn(viewModelScope)
    }


    class Factory(private val repository: StoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
