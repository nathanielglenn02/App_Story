package com.example.app_story

import androidx.lifecycle.MutableLiveData
import com.example.app_story.test.getOrAwaitValue
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    fun `test LiveData value`() {
        val liveData = MutableLiveData<String>()
        liveData.postValue("Hello World")

        val result = liveData.getOrAwaitValue()
        assertEquals("Hello World", result)
    }
}