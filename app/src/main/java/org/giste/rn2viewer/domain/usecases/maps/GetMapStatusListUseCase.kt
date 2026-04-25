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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.giste.rn2viewer.domain.model.MapStatus
import org.giste.rn2viewer.domain.model.MapWithStatus
import org.giste.rn2viewer.domain.repositories.LocalMapMetadataRepository
import org.giste.rn2viewer.domain.repositories.MapRepository
import javax.inject.Inject

class GetMapStatusListUseCase @Inject constructor(
    private val mapRepository: MapRepository,
    private val metadataRepository: LocalMapMetadataRepository
) {
    operator fun invoke(): Flow<List<MapWithStatus>> {
        return combine(
            mapRepository.getAvailableMaps(),
            mapRepository.getDownloadedMaps(),
            metadataRepository.getAllMetadata()
        ) { availableCategories, downloadedFiles, allMetadata ->
            val remoteMaps = availableCategories.flatMap { it.maps }
            val statusList = mutableListOf<MapWithStatus>()

            // Process remote maps (AVAILABLE, DOWNLOADED, UPDATABLE)
            remoteMaps.forEach { remote ->
                val fileName = remote.relativeUrl.substringAfterLast("/")
                val localFile = downloadedFiles.find { it.name == fileName }
                val metadata = allMetadata.find { it.mapId == remote.id }

                val status = when {
                    localFile == null -> MapStatus.AVAILABLE
                    metadata == null -> MapStatus.DOWNLOADED // Local exists but no metadata (legacy or manual copy)
                    remote.lastModified > metadata.serverLastModified -> MapStatus.UPDATABLE
                    else -> MapStatus.DOWNLOADED
                }

                statusList.add(
                    MapWithStatus(
                        remoteInfo = remote,
                        localFile = localFile,
                        metadata = metadata,
                        status = status
                    )
                )
            }

            // Process OUTDATED (Locally exists but not in remote list)
            val processedFileNames = remoteMaps.map { it.relativeUrl.substringAfterLast("/") }.toSet()
            downloadedFiles.filter { it.name !in processedFileNames }.forEach { local ->
                statusList.add(
                    MapWithStatus(
                        remoteInfo = null,
                        localFile = local,
                        metadata = allMetadata.find { it.fileName == local.name },
                        status = MapStatus.OUTDATED
                    )
                )
            }

            statusList.sortedBy { it.name }
        }
    }
}
