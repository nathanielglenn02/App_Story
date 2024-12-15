package com.example.app_story.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import com.example.app_story.model.Story
import com.example.app_story.repository.StoryRepository
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val storyRepository: StoryRepository = mockk()
    private lateinit var homeViewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        homeViewModel = HomeViewModel(storyRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cancel()
    }

    @Test
    fun `when getStories should return success`() = runTest {
        val dummyStories = listOf(
            Story("id1", "Story 1", "Description 1", "photoUrl1", -6.2000, 106.816666),
            Story("id2", "Story 2", "Description 2", "photoUrl2", -6.3000, 107.0000)
        )
        val pagingData = PagingData.from(dummyStories)

        coEvery { storyRepository.getStories(any()) } returns flow { emit(pagingData) }

        val result = homeViewModel.getStories("Bearer token")

        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(result)
        coVerify { storyRepository.getStories(any()) }
    }

    @Test
    fun `when getStories should return empty`() = runTest {
        val pagingData = PagingData.empty<Story>()

        coEvery { storyRepository.getStories(any()) } returns flow { emit(pagingData) }

        val result = homeViewModel.getStories("Bearer token")

        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(result)
        coVerify { storyRepository.getStories(any()) }
    }
}
