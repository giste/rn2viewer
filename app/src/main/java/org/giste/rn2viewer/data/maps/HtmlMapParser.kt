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

import org.giste.rn2viewer.domain.model.RemoteMapInfo
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Utility to parse HTML directory listings from Mapsforge mirror servers.
 */
object HtmlMapParser {

    /**
     * Extracts links and metadata from Apache/Nginx directory listing HTML.
     */
    fun parseDirectory(html: String, continent: String, parentPath: String = ""): DirectoryContent {
        val maps = mutableListOf<RemoteMapInfo>()
        val subFolders = mutableListOf<String>()

        // Split HTML by table rows
        val rows = html.split("<tr>", "</tr>", "\n")
        
        val linkRegex = """<a href="([^"]+)".*?>(.*?)</a>""".toRegex()
        val dateRegex = """(\d{4}-\d{2}-\d{2}\s\d{2}:\d{2})""".toRegex()
        val sizeRegex = """\s+([0-9.]+[KMG]|-)\s*""".toRegex()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        rows.forEach { row ->
            val linkMatch = linkRegex.find(row) ?: return@forEach
            val href = linkMatch.groupValues[1]

            if (href == "../" || href.contains("://")) return@forEach

            val dateStr = dateRegex.find(row)?.groupValues?.get(1)
            val sizeStr = sizeRegex.find(row)?.groupValues?.get(1) ?: "-"

            val timestamp = dateStr?.let { 
                try { dateFormat.parse(it)?.time } catch (_: Exception) { null }
            } ?: 0L

            if (href.endsWith(".map")) {
                val name = href.removeSuffix(".map").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                val relativeUrl = if (parentPath.isEmpty()) href else "$parentPath/$href"
                maps.add(
                    RemoteMapInfo(
                        id = relativeUrl.replace("/", "_").removeSuffix(".map"),
                        name = name,
                        relativeUrl = relativeUrl,
                        size = parseSize(sizeStr),
                        continent = continent,
                        lastModified = timestamp
                    )
                )
            } else if (href.endsWith("/")) {
                subFolders.add(href.removeSuffix("/"))
            }
        }

        // Apply Heuristic: If "X.map" exists, ignore folder "X/"
        val mapNames = maps.map { it.name.lowercase() }.toSet()
        val filteredFolders = subFolders.filter { it.lowercase() !in mapNames }

        return DirectoryContent(maps, filteredFolders)
    }

    /**
     * Converts mirror size strings (1.1G, 500M, 10K) to bytes.
     */
    private fun parseSize(sizeStr: String): Long {
        val trimmed = sizeStr.trim()
        if (trimmed.isEmpty() || trimmed == "-") return 0L
        return try {
            val unit = trimmed.last().uppercaseChar()
            if (unit.isDigit()) return trimmed.toLong()
            
            val value = trimmed.dropLast(1).toDouble()
            when (unit) {
                'G' -> (value * 1024.0 * 1024.0 * 1024.0).toLong()
                'M' -> (value * 1024.0 * 1024.0).toLong()
                'K' -> (value * 1024.0).toLong()
                else -> value.toLong()
            }
        } catch (_: Exception) {
            0L
        }
    }

    data class DirectoryContent(
        val maps: List<RemoteMapInfo>,
        val subFolders: List<String>
    )
}
