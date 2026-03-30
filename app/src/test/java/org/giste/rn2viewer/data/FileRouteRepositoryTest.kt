package org.giste.rn2viewer.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.giste.rn2viewer.domain.model.Route
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileRouteRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: FileRouteRepository
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = FileRouteRepository(context, testDispatcher)
    }

    @Test
    fun `saveRoute should write to file and loadRoute should read it back`() = runTest(testDispatcher) {
        // Given
        val route = Route(
            name = "Test Route",
            description = "Description",
            waypoints = emptyList()
        )

        // When
        repository.saveRoute(route)
        val loadedRoute = repository.loadRoute().first()

        // Then
        assertEquals(route.name, loadedRoute?.name)
        assertEquals(route.description, loadedRoute?.description)
    }

    @Test
    fun `loadRoute should return null if file does not exist`() = runTest(testDispatcher) {
        // When
        val loadedRoute = repository.loadRoute().first()

        // Then
        assertNull(loadedRoute)
    }
}
