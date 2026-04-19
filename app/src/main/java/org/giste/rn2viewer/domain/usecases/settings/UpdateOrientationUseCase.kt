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

package org.giste.rn2viewer.domain.usecases.settings

import org.giste.rn2viewer.domain.model.settings.AppOrientation
import org.giste.rn2viewer.domain.repositories.SettingsRepository
import javax.inject.Inject

class UpdateOrientationUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(orientation: AppOrientation) = repository.setOrientation(orientation)
}
