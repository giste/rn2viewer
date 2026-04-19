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

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.giste.rn2viewer.domain.model.settings.AppTheme
import org.giste.rn2viewer.domain.repositories.SettingsRepository
import org.junit.Test

class UpdateThemeUseCaseTest {

    private val repository: SettingsRepository = mockk()
    private val updateThemeUseCase = UpdateThemeUseCase(repository)

    @Test
    fun `invoke should call setTheme on repository`() = runTest {
        val theme = AppTheme.DARK
        coEvery { repository.setTheme(theme) } returns Unit

        updateThemeUseCase(theme)

        coVerify { repository.setTheme(theme) }
    }
}
