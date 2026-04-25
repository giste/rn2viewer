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
 * along with this program.  See <https://www.gnu.org/licenses/>.
 */

package org.giste.rn2viewer.data.maps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlMapParserTest {

    @Test
    fun `parseDirectory should extract maps and ignore redundant folders`() {
        // Arrange: Sample HTML with standard spacing
        val html = """
            <a href="albania/">albania/</a> 2024-03-10 10:00 -
            <a href="andorra.map">andorra.map</a> 2024-01-15 12:30 1.6M
            <a href="spain.map">spain.map</a> 2024-03-01 09:00 1.1G
            <a href="spain/">spain/</a> 2024-03-10 10:00 -
            <a href="us/">us/</a> 2024-03-10 10:00 -
            <a href="us-midwest.map">us-midwest.map</a> 2024-01-01 00:00 500M
        """.trimIndent()

        // Act
        val content = HtmlMapParser.parseDirectory(html, "Europe")

        // Assert: Maps extraction
        assertEquals(3, content.maps.size)
        assertTrue(content.maps.any { it.name == "Spain" })
        assertTrue(content.maps.any { it.name == "Andorra" })

        // Assert: Redundant folder filtering
        assertTrue(content.subFolders.contains("albania"))
        assertTrue(content.subFolders.contains("us"))
        assertTrue("Folder 'spain/' should be filtered out", !content.subFolders.contains("spain"))
    }

    @Test
    fun `parseSize should handle units correctly`() {
        val html = """
            <a href="g.map">g.map</a> 2024-01-01 00:00 1.5G
            <a href="m.map">m.map</a> 2024-01-01 00:00 500M
            <a href="k.map">k.map</a> 2024-01-01 00:00 10K
        """.trimIndent()
        
        val maps = HtmlMapParser.parseDirectory(html, "Test").maps
        
        assertEquals(1610612736L, maps.find { it.name == "G" }?.size)
        assertEquals(524288000L, maps.find { it.name == "M" }?.size)
        assertEquals(10240L, maps.find { it.name == "K" }?.size)
    }
}
