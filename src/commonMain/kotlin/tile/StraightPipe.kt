package tile

import Assets
import Direction
import Orientation
import korlibs.korge.view.SContainer
import korlibs.korge.view.Sprite
import korlibs.korge.view.centered
import korlibs.korge.view.image
import korlibs.korge.view.position
import korlibs.korge.view.rotation
import korlibs.korge.view.size
import korlibs.korge.view.sprite
import korlibs.math.geom.Angle
import korlibs.math.geom.Rectangle
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class StraightPipe(
    val length: Duration,
    val orientation: Orientation,
) : Tile() {
    var elapsed = 0.seconds
    var liquidDirection: Direction? = null
    var filled = false
    lateinit var liquidView: Sprite

    override fun bindView(
        target: Rectangle,
        assets: Assets,
        sContainer: SContainer,
    ) {
        release()
        views.add(
            sContainer.image(
                assets.empty,
            ) {
                position(target.centerX, target.centerY)
                centered
                size(target.size)
            },
        )
        views.add(
            sContainer.image(
                assets.straightV,
            ) {
                position(target.centerX, target.centerY)
                centered
                size(target.size)
                rotation(angle())
            },
        )
        liquidView =
            sContainer.sprite(
                assets.straightFluid,
            ) {
                position(target.centerX, target.centerY)
                centered
                size(target.size)
                setFrame(0)
                rotation(angle())
            }
        views.add(
            liquidView,
        )
    }

    private fun angle() =
        when (orientation) {
            Orientation.VERTICAL -> Angle.ZERO
            Orientation.HORIZONTAL -> Angle.QUARTER
        }

    override fun onUpdate(dt: Duration): TileEvent? {
        if (liquidDirection != null) {
            if (!filled) {
                elapsed += dt
            }

            val elapsedRatio = (elapsed / length).coerceAtMost(0.9999)
            val frame = (liquidView.totalFrames * elapsedRatio).toInt()
            liquidView.setFrame(frame)

            if (!filled && liquidDirection != null && elapsed > length) {
                filled = true
                return Overflow(
                    elapsed - length,
                    liquidDirection!!,
                )
            }
        }
        return null
    }

    override fun takeLiquid(
        direction: Direction,
        dt: Duration,
    ) = when {
        liquidDirection != null -> false
        direction in orientation.directions -> {
            liquidDirection = direction
            liquidView.rotation(
                when (direction) {
                    Direction.UP -> Angle.ZERO
                    Direction.DOWN -> Angle.HALF
                    Direction.LEFT -> Angle.THREE_QUARTERS
                    Direction.RIGHT -> Angle.QUARTER
                },
            )
            true
        }

        else -> false
    }

    override fun isEditable() = liquidDirection == null
}
