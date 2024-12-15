package com.example.app_story.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.app_story.model.Story
import com.example.app_story.network.ApiService
import com.example.app_story.paging.StoryPagingSource
import kotlinx.coroutines.flow.Flow

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
}
