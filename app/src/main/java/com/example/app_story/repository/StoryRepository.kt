package com.example.app_story.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.app_story.model.Story
import com.example.app_story.network.ApiService
import com.example.app_story.paging.StoryPagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class StoryRepository(private val apiService: ApiService) {

    fun getStories(token: String): Flow<PagingData<Story>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { StoryPagingSource(apiService, token) }
        ).flow
    }

    fun getStoriesWithLocation(token: String): Flow<List<Story>> = flow {
        val response = apiService.getStoriesWithLocation(token)
        emit(response.listStory) // Emit list cerita dengan lokasi
    }.flowOn(Dispatchers.IO)

}
