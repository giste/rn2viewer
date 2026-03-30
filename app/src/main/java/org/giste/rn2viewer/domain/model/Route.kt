package org.giste.rn2viewer.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Route(
    val name: String = "",
    val description: String = "",
    val startLocation: String = "",
    val endLocation: String = "",
    val tulips: List<Tulip>,
)
