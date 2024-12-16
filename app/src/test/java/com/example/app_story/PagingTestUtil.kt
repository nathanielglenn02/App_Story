package com.example.app_story.utils

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

suspend fun <T : Any> Flow<PagingData<T>>.collectDataForTest(): PagingData<T> {
    return this.first()
}
