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

package org.giste.rn2viewer.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.giste.rn2viewer.data.database.dao.LocalMapMetadataDao
import org.giste.rn2viewer.data.database.entity.LocalMapMetadataEntity
import org.giste.rn2viewer.domain.model.LocalMapMetadata
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LocalMapMetadataRepositoryTest {

    private lateinit var repository: RoomLocalMapMetadataRepository
    private val dao: LocalMapMetadataDao = mockk()

    @Before
    fun setup() {
        repository = RoomLocalMapMetadataRepository(dao)
    }

    @Test
    fun `getAllMetadata returns domain list from dao`() = runTest {
        // Arrange
        val entities = listOf(
            LocalMapMetadataEntity("1", "map1.map", 1000L, 1024L),
            LocalMapMetadataEntity("2", "map2.map", 2000L, 2048L)
        )
        every { dao.getAllMetadata() } returns flowOf(entities)

        // Act
        val result = repository.getAllMetadata().first()

        // Assert
        assertEquals(2, result.size)
        assertEquals("map1.map", result[0].fileName)
        assertEquals(1000L, result[0].serverLastModified)
    }

    @Test
    fun `getMetadataForMap returns domain model`() = runTest {
        // Arrange
        val entity = LocalMapMetadataEntity("1", "map1.map", 1000L, 1024L)
        coEvery { dao.getMetadataById("1") } returns entity

        // Act
        val result = repository.getMetadataForMap("1")

        // Assert
        assertEquals("map1.map", result?.fileName)
        assertEquals(1024L, result?.size)
    }

    @Test
    fun `saveMetadata inserts entity into dao`() = runTest {
        // Arrange
        val metadata = LocalMapMetadata("1", "map1.map", 1000L, 1024L)
        coEvery { dao.insertMetadata(any()) } returns Unit

        // Act
        repository.saveMetadata(metadata)

        // Assert
        coVerify { dao.insertMetadata(LocalMapMetadataEntity.fromDomain(metadata)) }
    }

    @Test
    fun `deleteMetadata calls dao delete`() = runTest {
        // Arrange
        coEvery { dao.deleteMetadata("1") } returns Unit

        // Act
        repository.deleteMetadata("1")

        // Assert
        coVerify { dao.deleteMetadata("1") }
    }
}
