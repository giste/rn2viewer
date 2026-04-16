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

package org.giste.rn2viewer.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class OdometerTest {

    @Test
    fun `initialization clamps negative values to zero`() {
        val odometer = Odometer(total = -100.0, partial = -50.0)
        
        assertEquals(0.0, odometer.total, 0.0)
        assertEquals(0.0, odometer.partial, 0.0)
    }

    @Test
    fun `initialization clamps values above maximum`() {
        val odometer = Odometer(
            total = Odometer.MAX_TOTAL_METERS + 100.0,
            partial = Odometer.MAX_PARTIAL_METERS + 100.0
        )
        
        assertEquals(Odometer.MAX_TOTAL_METERS, odometer.total, 0.0)
        assertEquals(Odometer.MAX_PARTIAL_METERS, odometer.partial, 0.0)
    }

    @Test
    fun `copy clamps new negative values to zero`() {
        val odometer = Odometer(total = 100.0, partial = 50.0)
        val updated = odometer.copy(partial = -10.0)
        
        assertEquals(100.0, updated.total, 0.0)
        assertEquals(0.0, updated.partial, 0.0)
    }

    @Test
    fun `copy clamps new values above maximum`() {
        val odometer = Odometer(total = 100.0, partial = 50.0)
        val updated = odometer.copy(total = Odometer.MAX_TOTAL_METERS + 1.0)
        
        assertEquals(Odometer.MAX_TOTAL_METERS, updated.total, 0.0)
        assertEquals(50.0, updated.partial, 0.0)
    }

    @Test
    fun `equality works for same values`() {
        val o1 = Odometer(10.0, 5.0)
        val o2 = Odometer(10.0, 5.0)
        
        assertEquals(o1, o2)
        assertEquals(o1.hashCode(), o2.hashCode())
    }
}
