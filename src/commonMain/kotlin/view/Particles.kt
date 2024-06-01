package view

import Assets
import korlibs.korge.scene.SceneContainer
import korlibs.korge.view.View
import korlibs.math.geom.Angle
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Vector2
import korlibs.math.geom.times
import korlibs.time.TimeSpan
import korlibs.time.seconds
import kotlin.time.Duration

class Particles(
    val assets: Assets,
    val sContainer: SceneContainer,
    val viewRectangle: Rectangle,
) {
    data class Particle(
        var position: Vector2,
        var velocity: Vector2,
        val acceleration: Vector2,
        var angle: Angle,
        val angularVelocity: Angle,
        val view: View,
        var lifetime: Duration,
    )

    private val particles = mutableListOf<Particle>()

    fun addParticle(particle: Particle) {
        particles.add(particle)
    }

    fun update(dt: TimeSpan) {
        val toRemove = mutableListOf<Particle>()
        particles.forEach {
            it.position += dt.seconds * it.velocity
            it.velocity += dt.seconds * it.acceleration
            it.angle += it.angularVelocity * dt.seconds

            it.lifetime -= dt

            it.view.x = it.position.x.toDouble()
            it.view.y = it.position.y.toDouble()
            it.view.rotation = it.angle

            if (it.lifetime <= 0.seconds) toRemove.add(it)
            if (!viewRectangle.contains(it.position)) toRemove.add(it)
        }

        toRemove.forEach {
            it.view.removeFromParent()
        }
        particles.removeAll(toRemove)
    }
}
