/*
 * Rn2 Viewer
 * Copyright (C) 2026  Giste
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.giste.rn2viewer.domain.usecases

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.giste.rn2viewer.domain.repositories.RouteRepository
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ImportRouteUseCaseTest {

    private val routeRepository = mockk<RouteRepository>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var importRouteUseCase: ImportRouteUseCase

    @Before
    fun setup() {
        importRouteUseCase = ImportRouteUseCase(routeRepository, testDispatcher)
    }

    @Test
    fun `invoke should fetch external content and save it raw`() = runTest {
        // Given
        val uriString = "content://path/to/file.rn2"
        val jsonString = """
            {
                "route": {
                    "version": 4,
                    "name": "Test Route",
                    "waypoints": []
                }
            }
        """.trimIndent()
        coEvery { routeRepository.getExternalRouteContent(uriString) } returns Result.success(jsonString)

        // When
        val result = importRouteUseCase(uriString)

        // Then
        assertTrue(result.isSuccess)
        coVerify { routeRepository.saveRouteRaw(jsonString) }
    }

    @Test
    fun `invoke should return failure when JSON is invalid`() = runTest {
        // Given
        val uriString = "content://path/to/file.rn2"
        val jsonString = "invalid json"
        coEvery { routeRepository.getExternalRouteContent(uriString) } returns Result.success(jsonString)

        // When
        val result = importRouteUseCase(uriString)

        // Then
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { routeRepository.saveRouteRaw(any()) }
    }

    @Test
    fun `invoke should return failure when extension is invalid`() = runTest {
        // Given
        val uriString = "content://path/to/file.txt"

        // When
        val result = importRouteUseCase(uriString)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        coVerify(exactly = 0) { routeRepository.getExternalRouteContent(any()) }
    }
}
