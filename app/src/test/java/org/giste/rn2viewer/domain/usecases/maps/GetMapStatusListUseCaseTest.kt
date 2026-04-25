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

package org.giste.rn2viewer.domain.usecases.maps

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.giste.rn2viewer.domain.model.LocalMapMetadata
import org.giste.rn2viewer.domain.model.MapCategory
import org.giste.rn2viewer.domain.model.MapFile
import org.giste.rn2viewer.domain.model.MapStatus
import org.giste.rn2viewer.domain.model.RemoteMapInfo
import org.giste.rn2viewer.domain.repositories.LocalMapMetadataRepository
import org.giste.rn2viewer.domain.repositories.MapRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetMapStatusListUseCaseTest {

    private lateinit var useCase: GetMapStatusListUseCase
    private val mapRepository: MapRepository = mockk()
    private val metadataRepository: LocalMapMetadataRepository = mockk()

    @Before
    fun setup() {
        useCase = GetMapStatusListUseCase(mapRepository, metadataRepository)
    }

    @Test
    fun `invoke classifies maps correctly`() = runTest {
        // Arrange
        val remoteSpain = RemoteMapInfo("spain", "Spain", "europe/spain.map", 100, "Europe", 2000L)
        val remoteFrance = RemoteMapInfo("france", "France", "europe/france.map", 100, "Europe", 2000L)
        val remoteItaly = RemoteMapInfo("italy", "Italy", "europe/italy.map", 100, "Europe", 2000L)
        
        val categories = listOf(MapCategory("Europe", listOf(remoteSpain, remoteFrance, remoteItaly)))
        
        val localSpain = MapFile("spain.map", "/path/spain.map", 100)
        val localFrance = MapFile("france.map", "/path/france.map", 100)
        val localPortugal = MapFile("portugal.map", "/path/portugal.map", 100)
        
        val metaSpain = LocalMapMetadata("spain", "spain.map", 2000L, 100)
        val metaFrance = LocalMapMetadata("france", "france.map", 1000L, 100) // Older than remote
        
        every { mapRepository.getAvailableMaps() } returns flowOf(categories)
        every { mapRepository.getDownloadedMaps() } returns flowOf(listOf(localSpain, localFrance, localPortugal))
        every { metadataRepository.getAllMetadata() } returns flowOf(listOf(metaSpain, metaFrance))

        // Act
        val result = useCase().first()

        // Assert
        // Spain: Downloaded (matches)
        assertEquals(MapStatus.DOWNLOADED, result.find { it.id == "spain" }?.status)
        // France: Updatable (remote 2000 > meta 1000)
        assertEquals(MapStatus.UPDATABLE, result.find { it.id == "france" }?.status)
        // Italy: Available (exists remotely but not locally)
        assertEquals(MapStatus.AVAILABLE, result.find { it.id == "italy" }?.status)
        // Portugal: Outdated (exists locally but not remotely)
        assertEquals(MapStatus.OUTDATED, result.find { it.id == "portugal.map" }?.status)
    }
}
