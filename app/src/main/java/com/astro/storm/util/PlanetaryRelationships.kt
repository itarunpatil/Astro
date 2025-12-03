package com.astro.storm.util

import com.astro.storm.data.model.Planet

object PlanetaryRelationships {

    enum class Relationship {
        FRIEND,
        NEUTRAL,
        ENEMY
    }

    private val friendships = mapOf(
        Planet.SUN to listOf(Planet.MOON, Planet.MARS, Planet.JUPITER),
        Planet.MOON to listOf(Planet.SUN, Planet.MERCURY),
        Planet.MARS to listOf(Planet.SUN, Planet.MOON, Planet.JUPITER),
        Planet.MERCURY to listOf(Planet.SUN, Planet.VENUS),
        Planet.JUPITER to listOf(Planet.SUN, Planet.MOON, Planet.MARS),
        Planet.VENUS to listOf(Planet.MERCURY, Planet.SATURN),
        Planet.SATURN to listOf(Planet.MERCURY, Planet.VENUS)
    )

    private val enemies = mapOf(
        Planet.SUN to listOf(Planet.VENUS, Planet.SATURN),
        Planet.MOON to emptyList<Planet>(),
        Planet.MARS to listOf(Planet.MERCURY),
        Planet.MERCURY to listOf(Planet.MOON),
        Planet.JUPITER to listOf(Planet.MERCURY, Planet.VENUS),
        Planet.VENUS to listOf(Planet.SUN, Planet.MOON),
        Planet.SATURN to listOf(Planet.SUN, Planet.MOON, Planet.MARS)
    )

    fun getRelationship(planet: Planet, otherPlanet: Planet): Relationship {
        return when {
            friendships[planet]?.contains(otherPlanet) == true -> Relationship.FRIEND
            enemies[planet]?.contains(otherPlanet) == true -> Relationship.ENEMY
            else -> Relationship.NEUTRAL
        }
    }
}
